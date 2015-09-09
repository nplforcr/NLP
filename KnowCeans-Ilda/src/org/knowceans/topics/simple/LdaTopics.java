/*
 * Created on Jan 24, 2010
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

import org.knowceans.corpus.CorpusResolver;
import org.knowceans.corpus.ILabelCorpus;
import org.knowceans.corpus.LabelNumCorpus;
import org.knowceans.util.IndexQuickSort;

/**
 * LdaTopics shows the topics of an LDA-like model
 * 
 * @author gregor
 */
public class LdaTopics {

    CorpusResolver cr;

    private double[][] phi;
    private double[][] theta;
    private LabelNumCorpus corpus;

    public LdaTopics(String filebase, double[][] theta, double[][] phi) {
        cr = new CorpusResolver(filebase);
        corpus = new LabelNumCorpus(filebase);
        this.theta = theta;
        this.phi = phi;
    }

    /**
     * create string with document topic information
     * 
     * @param m doc id in model
     * @param morig doc id in original corpus
     * @param topics how many of the most likely topics to print
     * @param label resolve category
     * @param author resolve authorship
     * @return
     */
    public String printDocument(int m, int morig, int topics, boolean label,
        boolean author) {
        StringBuffer b = new StringBuffer();
        b.append(String.format("%d: %s\n    id = %d\n", m, cr.getDoc(morig),
            morig));
        if (author) {
            int[][] a = corpus.getDocLabels(ILabelCorpus.LAUTHORS);
            StringBuffer aa = new StringBuffer();
            for (int i : a[morig]) {
                aa.append(cr.getAuthor(i)).append(" ");
            }
            b.append(String.format("    authors = %s\n", aa.toString()));
        }
        if (label) {
            int[][] a = corpus.getDocLabels(ILabelCorpus.LCATEGORIES);
            b.append(String.format("    cat = %s\n", cr.getLabel(a[morig][0])));
        }
        int[] a = IndexQuickSort.sort(theta[m]);
        IndexQuickSort.reverse(a);
        for (int k = 0; k < Math.min(a.length, topics); k++) {
            int kk = a[k];
            b.append(String.format("    %d (%2.5f):%s\n", kk, theta[m][kk],
                printTopic(kk, 10)));
        }
        return b.toString();

    }

    public String printTopic(int k, int terms) {
        StringBuffer b = new StringBuffer();
        int[] a = IndexQuickSort.sort(phi[k]);
        IndexQuickSort.reverse(a);
        for (int t = 0; t < Math.min(a.length, terms); t++) {
            int tt = a[t];
            b.append(String.format(" %d %s (%2.5f)", tt, cr.getTerm(tt),
                phi[k][tt]));
        }
        return b.toString();
    }

}
