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

import static java.lang.Math.log;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.knowceans.corpus.NumCorpus;
import org.knowceans.corpus.VisCorpus;
import org.knowceans.util.ArrayIo;
import org.knowceans.util.ArrayUtils;
import org.knowceans.util.CokusRandom;
import org.knowceans.util.DirichletEstimation;
import org.knowceans.util.IndexQuickSort;
import org.knowceans.util.RandomSamplers;
import org.knowceans.util.StopWatch;
import org.knowceans.util.Vectors;

/**
 * LDA Gibbs sampler with nonparametric prior (HDP):
 * <p>
 * (m,k | alpha * tau | gamma), k->inf, (k,t | beta)
 * <p>
 * using Teh et al. (2006) approach for the direct assignment sampler, with
 * modular LDA parametric sampler first published by Griffiths (2002) and
 * explained in Heinrich (2005). For the original LDA paper, see Blei et al.
 * (2002).
 * <p>
 * The general idea is to retain as much as possible of the standard LDA Gibbs
 * sampler, which is possible by alternatingly sampling the finite case with K +
 * 1 topics and resampling the topic weights taking into account the current
 * assignments of data items to topics and pruning or expanding the topic set
 * accordingly.
 * <p>
 * I tried to find the (subjectively) best tradeoff between simplicity and the
 * JASA paper (Teh et al. 2006). Therefore I have only used the direct
 * assignment method.
 * <p>
 * The implementation uses lists instead of primitive arrays, but for
 * performance reasons, this may be changed to have a bound Kmax to allocate
 * fixed-size arrays, similar to a truncated DP.
 * <p>
 * Caveats: (1) Performance is not a core criterion, and OOP encapsulation is
 * ignored for compactness' sake. (2) Code still uses the likelihood function of
 * LDA, and without the hyperparameter terms.
 * <p>
 * LICENSE: GPL3, see: http://www.gnu.org/licenses/gpl-3.0.html
 * <p>
 * References:
 * <p>
 * D.M. Blei, A. Ng, M.I. Jordan. Latent Dirichlet Allocation. NIPS, 2002
 * <p>
 * T. Griffiths. Gibbs sampling in the generative model of Latent Dirichlet
 * Allocation. TR, 2002, www-psych.stanford.edu/~gruffydd/cogsci02/lda.ps
 * <p>
 * G. Heinrich. Parameter estimation for text analysis. TR, 2009,
 * www.arbylon.net/publications/textest2.pdf
 * <p>
 * G. Heinrich. "Infinite LDA" -- implementing the HDP with minimum code
 * complexity. TN2011/1, www.arbylon.net/publications/ilda.pdf
 * <p>
 * Y.W. Teh, M.I. Jordan, M.J. Beal, D.M. Blei. Hierarchical Dirichlet
 * Processes. JASA, 101:1566-1581, 2006
 * 
 * @author (c) 2008-2011 Gregor Heinrich, gregor :: arbylon : net
 * @version 0.95
 */
public class IldaGibbs implements ISimpleGibbs, ISimpleQueryGibbs, ISimplePpx {

	private static TopicMatrixPanel vis;

	/**
	 * test driver for mixture network Gibbs sampler
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		int niter = 1500, niterq = 10;
		String filebase = "nips/nips";
		// file or synthetic
		boolean usefile = false;
		// topic display panel
		boolean display = true;
        
		Random rand = new CokusRandom(56567651);  //56567651
		NumCorpus corpus;
		if (!usefile) {
			corpus = new NumCorpus(filebase + ".corpus");
			if (display) {
				// panel is K x K terms large, so let's use the sqrt.
				vis = new TopicMatrixPanel(900, 400, (int) Math.sqrt(corpus
						.getNumTerms()), 1);
			}

		} else {
			// test with generated corpus
			int K = 10;
			corpus = VisCorpus.generateLdaCorpus(K, 1000, 200);
			if (display) {
				vis = new TopicMatrixPanel(900, 400, K, 300 / K);
			}
		}

		// corpus.reduce(100, rand);
		corpus.split(10, 2, rand);
		NumCorpus train = (NumCorpus) corpus.getTrainCorpus();
		NumCorpus test = (NumCorpus) corpus.getTestCorpus();
		int[][] w = train.getDocWords(rand);      //train file->word to term
		int[][] wq = test.getDocWords(rand);      //test  file->word to term
		int K0 = 0;
		int V = corpus.getNumTerms();
		double alpha = 1.;
		// beta = 1 --> K = 12,.5-->16, .1-->26@200, 75@500, 115@645 (beta
		// should be larger),
		//
		double beta = .1;
		double gamma = 1.5; 

		// run sampler
		IldaGibbs gs = new IldaGibbs(w, wq, K0, V, alpha, beta, gamma, rand);
		gs.init();
		System.out.println("initialised");
		System.out.println(gs);

		// initial test
		gs.initq();
		gs.runq(niterq);
		System.out.println("perplexity = " + gs.ppx());

		StopWatch.start();
		System.out.println("starting Gibbs sampler with " + niter
				+ " iterations");

		gs.run(niter);
		System.out.println(StopWatch.format(StopWatch.stop()));

		// test
		gs.initq();
		gs.runq(niterq);
		System.out.println("perplexity = " + gs.ppx());
		System.out.println(gs);

		gs.packTopics();

		System.out.println("finished");
		System.out.println(gs);
		if (!usefile)
			try {
				PrintStream bw = new PrintStream(filebase + ".ilda.result");
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
				phi[k][t] = (nkt.get(k)[t] + beta) / (nk.get(k) + beta * V);
			}
		}
		for (int m = 0; m < M; m++) {
			for (int k = 0; k < K; k++) {
				theta[m][k] = (nmk[m].get(k) + alpha)
						/ (w[m].length + alpha * K);
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
			out.println(String.format("%d (%2.5f / %d): %s", k, nk.get(kk[k])
					/ (double) W * K, K, lt.printTopic(kk[k], 20)));
		}
		ArrayIo.saveBinaryMatrix(filebase + ".ilda.theta.zip", theta);
		ArrayIo.saveBinaryMatrix(filebase + ".ilda.phi.zip", phi);
	}

	private int[][] w;
	private int[][] wq;
	/**
	 * inactive components: index = index in count arrays, element = value in z.
	 * Filled on component removal.
	 */
	// reuse the component emptied last
	// private List<Integer> kgaps;
	// reuse component with the lowest index
	private SortedSet<Integer> kgaps;
	/**
	 * active components: value = value in z and count arrays, which should
	 * always match. This array is never removed elements from but inactive
	 * elements are set to -1. This allows to reuse components (gaps) and to
	 * keep the indices of z and counts identical.
	 */
	private List<Integer> kactive;
	private List<Integer>[] nmk;
	private int[][] nmkq;
	private List<int[]> nkt;
	private List<Integer> nk;
	private double[][] phi;
	private int[][] z;
	private int[][] zq;

	private double[] pp;
	/**
	 * step to increase the sampling array
	 */
	public final int ppstep = 10;
	/**
	 * precision of the 2nd-level DP
	 */
	private double alpha;
	/**
	 * mean of the 2nd-level DP = sample from 1st-level DP
	 */
	private ArrayList<Double> tau;
	/**
	 * parameter of root base measure (= component Dirichlet)
	 */
	private double beta;
	/**
	 * precision of root DP
	 */
	private double gamma;

	// hyperparameters for DP and Dirichlet samplers
	// Teh+06: Docs: (1, 1), M1-3: (0.1, 0.1); HMM: (1, 1)
	double aalpha = 5;
	double balpha = 0.1;
	double abeta = 0.1;
	double bbeta = 0.1;
	// Teh+06: Docs: (1, 0.1), M1-3: (5, 0.1), HMM: (1, 1)
	double agamma = 5;
	double bgamma = 0.1;
	// number of samples for parameter samplers
	int R = 10;

	/**
	 * total number of tables
	 */
	private double T;

	private Random rand;
	RandomSamplers samp;
	private int iter;
	/**
	 * current number of non-empty components
	 */
	private int K;
	private int M;
	private int Mq;
	private int Wq;
	private int V;
	private boolean inited = false;
	private boolean fixedK = false;
	private boolean fixedHyper = false;

	/**
	 * parametrise gibbs sampler
	 * 
	 * @param w
	 *            word tokens
	 * @param wq
	 *            word tokens (testing)
	 * @param K
	 *            initial number of topics: may be 0 if gamma > 0.
	 * @param V
	 *            number of terms
	 * @param alpha
	 *            node A precision (document DP)
	 * @param gamma
	 *            node A precision (root DP), 0 for fixed K: plain LDA.
	 * @param beta
	 *            node B hyperparam
	 * @param rand
	 *            random number generator
	 */
	public IldaGibbs(int[][] w, int[][] wq, int K, int V, double alpha,
			double beta, double gamma, Random rand) {
		// assign
		this.w = w;
		this.wq = wq;
		// start with 0 or more topics
		this.K = K;
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
		if (gamma == 0) {
			this.fixedK = true;
		}
		this.M = w.length;
		this.Mq = wq.length;
		this.V = V;
		this.rand = rand;
		this.samp = new RandomSamplers(rand);
	}

	/**
	 * initialise Markov chain
	 */
	@SuppressWarnings("unchecked")
	public void init() {
		// allocate
		nmk = new ArrayList[M];
		nkt = new ArrayList<int[]>();
		nk = new ArrayList<Integer>();
		z = new int[M][];
		nk.add(0);  //added by Hao
		for (int m = 0; m < M; m++) {
			nmk[m] = new ArrayList<Integer>();
			for (int k = 0; k < K; k++) {
				nmk[m].add(0);
			}
			z[m] = new int[w[m].length];
		}
		// indexing lists
		kactive = new ArrayList<Integer>();
		// kgaps = new ArrayList<Integer>();
		kgaps = new TreeSet<Integer>();
		// create mean weights
		tau = new ArrayList<Double>();
		for (int k = 0; k < K; k++) {
			kactive.add(k);
			nkt.add(new int[V]);
			nk.add(0);
			// set to value for fixed K
			tau.add(1. / K);
		}
		// tau has one dimension more
		tau.add(1. / K);
		pp = new double[K + ppstep];
		// initialise (run without decrements because z[*][*] = -1)
		run(1);
		if (!fixedK) {
			updateTau();
		}
		inited = true;
	}

	/**
	 * initialise Markov chain for querying
	 */
	public void initq() {
		// compute parameters
		int Kg = K + kgaps.size();
		phi = new double[Kg][V];
		for (int kk = 0; kk < K; kk++) {
			int k = kactive.get(kk);
			for (int t = 0; t < V; t++) {
				phi[k][t] = (nkt.get(k)[t] + beta) / (nk.get(k) + V * beta);
			}
		}
		// allocate
		nmkq = new int[Mq][Kg];
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
	 *     number of Gibbs iterations
	 */
	public void run(int niter) {

		for (iter = 0; iter < niter; iter++) {
			System.out.println(iter);
			System.out.println(this);
			for (int m = 0; m < M; m++) {
				for (int n = 0; n < w[m].length; n++) {
					// sampling z
					int k, kold = -1;
					int t = w[m][n];
					if (inited) {
						k = z[m][n];
						// decrement
						nmk[m].set(k, nmk[m].get(k) - 1);
						nkt.get(k)[t]--;
						nk.set(k, nk.get(k) - 1);
						kold = k;
					}
					// compute weights
					double psum = 0;
					// (37)
					for (int kk = 0; kk < K; kk++) {
						k = kactive.get(kk);
						//(nmk[m].get(k) + alpha * tau.get(k))
						//--------------------------- z = z_old ----------------//
						pp[kk] = nmk[m].get(k) / (nk.get(k) + alpha * tau.get(k)); 
						psum += pp[kk];
					}
					// likelihood of new component
					if (!fixedK) {
						//--------------------------- z = z_new ----------------//
						pp[K] = alpha * tau.get(K) / V;                           
						psum += pp[K];
					}
					double u = rand.nextDouble();
					u *= psum;
					psum = 0;
					int kk = 0;
					for (; kk < K + 1; kk++) {
						psum += pp[kk];
						if (u <= psum) {
							break;
						}
					}
					// reassign and increment
					if (kk < K) {
						k = kactive.get(kk);
						z[m][n] = k;
						nmk[m].set(k, nmk[m].get(k) + 1);
						nkt.get(k)[t]++;
						nk.set(k, nk.get(k) + 1);
					} else {
						assert (!fixedK);
						z[m][n] = spawnTopic(m, t);
						updateTau();
						System.out.println("K = " + K);
					}
					// empty topic?
					if (inited && nk.get(kold) == 0) {
						// remove the object not the index
						kactive.remove((Integer) kold);
						kgaps.add(kold);
						assert (Vectors.sum(nkt.get(kold)) == 0
								&& nk.get(kold) == 0 && nmk[m].get(kold) == 0);
						K--;
						System.out.println("K = " + K);
						updateTau();
					}
				} // n
			} // m
			if (vis != null) {
				vis.setTopics(nkt);
			}
			if (!fixedK) {
				updateTau();
			}
			if (iter > 10 && !fixedHyper) {
				updateHyper();
			}
		} // i
	}

	/**
	 * query Gibbs sampler. This assumes the standard LDA model as we know the
	 * dimensionality from the training set, therefore topics need to be pruned.
	 * 
	 * @param niter
	 *            number of Gibbs iterations
	 */
	public void runq(int niter) {
		for (int qiter = 0; qiter < niter; qiter++) {
			for (int m = 0; m < nmkq.length; m++) {
				for (int n = 0; n < wq[m].length; n++) {
					// decrement
					int k = zq[m][n];
					int t = wq[m][n];
					nmkq[m][k]--;
					// compute weights
					double psum = 0;
					for (int kk = 0; kk < K; kk++) {
						pp[kk] = (nmkq[m][kk] + alpha) * phi[kk][t];
						psum += pp[kk];
					}
					// sample
					double u = rand.nextDouble() * psum;
					psum = 0;
					int kk = 0;
					for (; kk < K; kk++) {
						psum += pp[kk];
						if (u <= psum) {
							break;
						}
					}
					// reassign and increment
					zq[m][n] = kk;
					nmkq[m][kk]++;
				} // n
			} // m
		} // i
	}

	/**
	 * adds a topic to the list of active topics, either by reusing an existing
	 * inactive index (gap) or increasing the count arrays. NB: Within this
	 * method, the state is inconsistent.
	 * 
	 * @param m
	 *            current document
	 * @param t
	 *            current term
	 * @return index of topic spawned
	 */
	private int spawnTopic(int m, int t) {
		int k;
		if (kgaps.size() > 0) {
			// reuse gap
			// k = kgaps.remove(kgaps.size() - 1);
			k = kgaps.first();
			kgaps.remove(k);
			kactive.add(k);
			nmk[m].set(k, 1);
			nkt.get(k)[t] = 1;
			nk.set(k, 1);
		} else {
			// add element to count arrays
			k = K;
			for (int i = 0; i < M; i++) {
				nmk[i].add(0);
			}
			kactive.add(K);
			nmk[m].set(K, 1);
			nkt.add(new int[V]);
			nkt.get(K)[t] = 1;
			nk.add(1);
			tau.add(0.);
		}
		K++;
		if (pp.length <= K) {
			pp = new double[K + ppstep];
		}
		return k;
	}

	/**
	 * reorders topics such that no gaps exist in the count arrays and topics
	 * are ordered with their counts descending. Removes any gap dimensions.
	 */
	public void packTopics() {
		// sort topics by size
		int[] knew2k = IndexQuickSort.sort(nk);
		IndexQuickSort.reverse(knew2k);
		// reorder and weed out empty count arrays
		IndexQuickSort.reorder(nk, knew2k);
		IndexQuickSort.reorder(nkt, knew2k);
		for (int i = 0; i < kgaps.size(); i++) {
			nk.remove(nk.size() - 1);
			nkt.remove(nkt.size() - 1);
		}
		for (int m = 0; m < M; m++) {
			IndexQuickSort.reorder(nmk[m], knew2k);
			for (int i = 0; i < kgaps.size(); i++) {
				nmk[m].remove(nmk[m].size() - 1);
			}
		}
		// any new topics will be appended
		kgaps.clear();
		int[] k2knew = IndexQuickSort.inverse(knew2k);
		// rewrite topic labels
		for (int i = 0; i < K; i++) {
			kactive.set(i, k2knew[kactive.get(i)]);
		}
		for (int m = 0; m < M; m++) {
			for (int n = 0; n < w[m].length; n++) {
				z[m][n] = k2knew[z[m][n]];
			}
		}
	}

	/**
	 * prune topics and update tau, the root DP mixture weights.
	 */
	private void updateTau() {
		// (40) sample mk
		double[] mk = new double[K + 1];
		// TODO: average multi-sample?
		for (int kk = 0; kk < K; kk++) {
			int k = kactive.get(kk);
			for (int m = 0; m < M; m++) {
				if (nmk[m].get(k) > 1) {
					// number of tables a CRP(alpha tau) produces for nmk items
					mk[kk] += samp.randAntoniak(alpha * tau.get(k), //
							nmk[m].get(k));
				} else {
					mk[kk] += nmk[m].get(k);
				}
			}
		}
		// number of tables
		T = Vectors.sum(mk);
		mk[K] = gamma;
		// (36) sample tau
		double[] tt = samp.randDir(mk);
		for (int kk = 0; kk < K; kk++) {
			int k = kactive.get(kk);
			tau.set(k, tt[kk]);
		}
		tau.set(K, tt[K]);
	}

	/**
	 * update scalar DP hyperparameters alpha, gamma and Dirichlet
	 * hyperparameter beta. Assumes that T is updated (by updateTau).
	 */
	private void updateHyper() {
		for (int r = 0; r < R; r++) {
			// gamma: root level (Escobar+West95) with n = T
			// (14)
			double eta = samp.randBeta(gamma + 1, T);
			double bloge = bgamma - log(eta);
			// (13')
			double pie = 1. / (1. + (T * bloge / (agamma + K - 1)));
			// (13)
			int u = samp.randBernoulli(pie);
			gamma = samp.randGamma(agamma + K - 1 + u, 1. / bloge);

			// alpha: document level (Teh+06)
			double qs = 0;
			double qw = 0;
			for (int m = 0; m < M; m++) {
				// (49) (corrected)
				qs += samp.randBernoulli(w[m].length / (w[m].length + alpha));
				// (48)
				qw += log(samp.randBeta(alpha + 1, w[m].length));
			}
			// (47)
			alpha = samp.randGamma(aalpha + T - qs, 1. / (balpha - qw));
		}
		int[] ak = (int[]) ArrayUtils.asPrimitiveArray(nk);
		int[][] akt = new int[K][V];
		for (int k = 0; k < K; k++) {
			akt[k] = nkt.get(k);
		}
		beta = DirichletEstimation
				.estimateAlphaMap(akt, ak, beta, abeta, bbeta);
	}

	/**
	 * @return the perplexity of the last query sample.
	 */
	public double ppx() {
		// TODO: this uses LDA's perplexity --> add hyperparameters and DP stuff
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

	// ////////////// output routines ////////////////

	/**
	 * assemble a string of overview information.
	 */
	@Override
	public String toString() {
		return String.format("ILDA: M = %d, K = %d, V = %d, "
				+ "alpha = %2.5f, beta = %2.5f, gamma = %2.5f", //
				M, K, V, alpha, beta, gamma);
	}
}
