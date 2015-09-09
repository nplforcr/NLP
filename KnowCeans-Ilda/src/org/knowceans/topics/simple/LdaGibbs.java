/*
 * Created on Jan 5, 2010
 */
/*
 * (C) Copyright 2005-2011, Gregor Heinrich (gregor :: arbylon : net) \
 * (This file is part of the knowceans-ilda experimental software package
 */
/*
 * knowceans-ilda is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU General Public License as published by the Free 
 * Software Foundation; either version 3 of the License, or (at your option) 
 * any later version.
 */
/*
 * knowceans-ilda is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS 
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 */
/*
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place, Suite 330, Boston, MA 02111-1307 USA
 */
package org.knowceans.topics.simple;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Random;

import org.knowceans.corpus.NumCorpus;
import org.knowceans.corpus.VisCorpus;
import org.knowceans.util.ArrayIo;
import org.knowceans.util.CokusRandom;
import org.knowceans.util.IndexQuickSort;
import org.knowceans.util.StopWatch;
import org.knowceans.util.Vectors;

/**
 * LDA Gibbs sampler: (m,k | alpha), (k,t | beta)
 * <p>
 * using standard linear-scan sampler
 * 
 * @author gregor
 */
public class LdaGibbs implements ISimpleGibbs, ISimpleQueryGibbs, ISimplePpx {

	private static TopicMatrixPanel vis;

	/**
	 * test driver for mixture network Gibbs sampler
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		int niter = 500, niterq = 10;
		int K = 50;
		String filebase = "nips/nips";
		// file or synthetic
		boolean usefile = true;
		// topic display panel
		boolean display = true;

		Random rand = new CokusRandom(56567651);
		NumCorpus corpus;
		if (usefile) {
			corpus = new NumCorpus(filebase + ".corpus");
			if (display) {
				// panel is K x K terms large, so let's use the sqrt.
				vis = new TopicMatrixPanel(900, 400, (int) Math.sqrt(corpus
						.getNumTerms()), 1);
			}
		} else {
			// test with generated corpus
			corpus = VisCorpus.generateLdaCorpus(K, 1000, 200);
			if (display) {
				vis = new TopicMatrixPanel(900, 400, K, 300 / K);
			}
		}
		// corpus.reduce(100, rand);
		corpus.split(10, 2, rand);
		NumCorpus train = (NumCorpus) corpus.getTrainCorpus();
		NumCorpus test = (NumCorpus) corpus.getTestCorpus();
		int[][] w = train.getDocWords(rand);
		int[][] wq = test.getDocWords(rand);
		int V = corpus.getNumTerms();
		double alpha = 0.1;
		double beta = 0.1;

		// run sampler
		LdaGibbs gs = new LdaGibbs(w, wq, K, V, alpha, beta, rand);
		gs.init();
		System.out.println(gs);

		// initial test
		gs.initq();
		gs.runq(niterq);
		System.out.println(gs.ppx());

		StopWatch.start();
		gs.run(niter);
		System.out.println(StopWatch.format(StopWatch.stop()));

		// test
		gs.initq();
		gs.runq(niterq);
		System.out.println(gs.ppx());
		if (vis == null)
			try {
				PrintStream bw = new PrintStream(filebase + ".lda.result");
				gs.print(bw, filebase, corpus.getOrigDocIds()[0],
						train.getNumWords());
				bw.close();
				System.out.println("done");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

	} // main

	/**
	 * print the result of the model, save topics to files
	 * 
	 * @param filebase
	 * @param docids
	 * @param W
	 */
	private void print(PrintStream out, String filebase, int[] docids, int W) {
		phi = new double[K][V];
		double[][] theta = new double[M][K];
		for (int k = 0; k < K; k++) {
			for (int t = 0; t < V; t++) {
				phi[k][t] = (nkt[k][t] + beta) / (nk[k] + beta * V);
			}
		}
		for (int m = 0; m < M; m++) {
			for (int k = 0; k < K; k++) {
				theta[m][k] = (nmk[m][k] + alpha) / (w[m].length + alpha * K);
			}
		}
		LdaTopics lt = new LdaTopics(filebase, theta, phi);
		for (int m = 0; m < M; m++) {
			// no labels
			out.println(lt.printDocument(m, docids[m], 10, false, true));
			// out.println(lt.printDocument(m, docids[m], 10, true, true));
		}
		int[] kk = IndexQuickSort.sort(nk);
		IndexQuickSort.reverse(kk);
		for (int k = 0; k < K; k++) {
			out.println(String.format("%d (%2.5f / %d): %s", k, nk[kk[k]]
					/ (double) W * K, K, lt.printTopic(kk[k], 20)));
		}
		ArrayIo.saveBinaryMatrix(filebase + ".lda.theta.zip", theta);
		ArrayIo.saveBinaryMatrix(filebase + ".lda.phi.zip", phi);
	}

	private int[][] w;
	private int[][] wq;
	private int[][] nmk;
	private int[][] nmkq;
	private int[][] nkt;
	private int[] nk;
	private double[][] phi;
	private int[][] z;
	private int[][] zq;
	private double alpha;
	private double beta;
	private Random rand;
	private int iter;
	private int K;
	private int M;
	private int Mq;
	private int Wq;
	private int V;

	/**
	 * parametrise gibbs sampler
	 * 
	 * @param w
	 *            word tokens
	 * @param wq
	 *            word tokens (testing)
	 * @param K
	 *            number of topics
	 * @param V
	 *            number of terms
	 * @param alpha
	 *            node A hyperparam
	 * @param beta
	 *            node B hyperparam
	 * @param rand
	 *            random number generator
	 */
	public LdaGibbs(int[][] w, int[][] wq, int K, int V, double alpha,
			double beta, Random rand) {
		// assign
		this.w = w;
		this.wq = wq;
		this.K = K;
		this.M = w.length;
		this.Mq = wq.length;
		this.V = V;
		this.alpha = alpha;
		this.beta = beta;
		this.rand = rand;
	}

	/**
	 * initialise Markov chain
	 */
	public void init() {
		// allocate
		nmk = new int[M][K];
		nkt = new int[K][V];
		nk = new int[K];
		z = new int[M][];
		// initialise
		for (int m = 0; m < M; m++) {
			z[m] = new int[w[m].length];
			for (int n = 0; n < w[m].length; n++) {
				int k = rand.nextInt(K);
				z[m][n] = k;
				nmk[m][k]++;
				nkt[k][w[m][n]]++;
				nk[k]++;
			}
		}
	}

	/**
	 * initialise Markov chain for querying
	 */
	public void initq() {
		// compute parameters
		phi = new double[K][V];
		for (int k = 0; k < K; k++) {
			for (int t = 0; t < V; t++) {
				phi[k][t] = (nkt[k][t] + beta) / (nk[k] + V * beta);
			}
		}
		// allocate
		nmkq = new int[Mq][K];
		zq = new int[Mq][];
		Wq = 0;
		// initialise
		for (int m = 0; m < Mq; m++) {
			zq[m] = new int[wq[m].length];
			for (int n = 0; n < wq[m].length; n++) {
				int k = rand.nextInt(K);
				zq[m][n] = k;
				nmkq[m][k]++;
				Wq++;
			}
		}
	}

	/**
	 * run Gibbs sampler
	 * 
	 * @param niter
	 *            number of Gibbs iterations
	 */
	public void run(int niter) {

		double[] pp = new double[K];
		for (iter = 0; iter < niter; iter++) {
			System.out.println(iter);
			for (int m = 0; m < M; m++) {
				for (int n = 0; n < w[m].length; n++) {
					// decrement
					int k = z[m][n];
					int t = w[m][n];
					nmk[m][k]--;
					nkt[k][t]--;
					nk[k]--;
					// compute weights
					double psum = 0;
					for (int kk = 0; kk < pp.length; kk++) {
						pp[kk] = (nmk[m][kk] + alpha) * //
								(nkt[kk][t] + beta) / (nk[kk] + V * beta); 
						psum += pp[kk];
					}
					// sample
					double u = rand.nextDouble() * psum;
					psum = 0;
					int kk = 0;
					for (kk = 0; kk < pp.length; kk++) {
						psum += pp[kk];
						if (u <= psum) {
							break;
						}
					}
					// reassign and increment
					z[m][n] = kk;
					nmk[m][kk]++;
					nkt[kk][t]++;
					nk[kk]++;
				} // n
			} // m
			if (vis != null)
				vis.setTopics(nkt);
		} // i
	}

	/**
	 * query Gibbs sampler
	 * 
	 * @param niter
	 *            number of Gibbs iterations
	 */
	public void runq(int niter) {

		double[] pp = new double[K];
		for (int qiter = 0; qiter < niter; qiter++) {
			for (int m = 0; m < nmkq.length; m++) {
				for (int n = 0; n < wq[m].length; n++) {
					// decrement
					int k = zq[m][n];
					int t = wq[m][n];
					nmkq[m][k]--;
					// compute weights
					double psum = 0;
					for (int kk = 0; kk < pp.length; kk++) {
						pp[kk] = (nmkq[m][kk] + alpha) * phi[kk][t];
						psum += pp[kk];
					}
					// sample
					double u = rand.nextDouble() * psum;
					psum = 0;
					int kk = 0;
					for (kk = 0; kk < pp.length; kk++) {
						psum += pp[kk];
						if (u <= psum) {
							break;
						}
					}
					// reassign and increment
					zq[m][n] = kk;
					try {
						nmkq[m][kk]++;
					} catch (Exception e) {
						System.out.println(Vectors.print(pp));
					}
				} // n
			} // m
		} // i
	}

	/**
	 * @return the perplexity of the last query sample
	 */
	public double ppx() {
		double loglik = 0;
		// compute thetaq
		double[][] thetaq = new double[Mq][K];
		for (int m = 0; m < Mq; m++) {
			for (int k = 0; k < K; k++) {
				thetaq[m][k] = (nmkq[m][k] + alpha)
						/ (wq[m].length + K * alpha);
			}
		}
		// compute ppx
		for (int m = 0; m < Mq; m++) {
			for (int n = 0; n < wq[m].length; n++) {
				double sum = 0;
				for (int k = 0; k < K; k++) {
					sum += thetaq[m][k] * phi[k][wq[m][n]];
				}
				loglik += Math.log(sum);
			}
		}
		return Math.exp(-loglik / Wq);
	}

	@Override
	public String toString() {
		return String.format(
				"LDA: M = %d, K = %d, V = %d, alpha = %2.5f, beta = %2.5f", M,
				K, V, alpha, beta);
	}
}
