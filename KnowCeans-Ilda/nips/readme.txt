This is the NIPS0-12 test collection

@author gregor :: arbylon . net
@date 2010-08-23 (release), 2010-11-25 added citation structure
  
To obtain a corpus with various different co-occurrence types is usually difficult. The NIPS corpus
as assembled here has, however, such a wide variety: Full word content, full authorship, vocabulary,
citation graph as well as category labels. By adding the internal citation data, the community
structure can be investigated, starting at the early beginnings, when the researchers did naturally
only cite themselves a few times -- interestingly, there are cross-citations even in the first volume
-- and then more and more forming an internal community that also changes its thematic focus as the
conference develops: from neural networks to support vector machines and statistical methods like
bayesian networks. This re-enactable change of focus has also the advantage that analysis can be 
intuitively verified. Although in this process, the co-citation between articles of the conference 
intensifies, the major portion of academic communication still goes through other conferences and
journals, which is not a surprise in an interdisciplinary field like the one NIPS addresses. In order
to enhance the internal structure of the citation graph, a second dimension of co-citation is added: 
Articles that mention members of the community are linked to those ones written by the people
mentioned. A third dimension, external co-citation, was too difficult to set up cleanly, as already
the internal citation recognition required a good amount of adjustment work in addition to the auto-
matic process.


Contents and line format: 
 - nips.authors        -- document author ids, 1 line per document (line 1 = document 0), 
                          space-separated
 - nips.authors.key    -- author names, 1 line per author (line 1 = author 0)
 - nips.corpus         -- term vectors, 1 line per document, svm-light line format: 
                          nterms (termid:termfreq)+
 - nips.docs           -- document titles, 1 line per document (line 1 = document 0)
 - nips.labels         -- class labels, 1 line per document (line 1 = document 0)
 - nips.labels.extract -- class merging information (original to final labels)
 - nips.label.key      -- class label names, 1 line per label (line 1 = class 0)
 - nips.split          -- permutation of the document set, can be used for random splitting
 - nips.vocab          -- vocabulary index, 1 line per term (line 1 = term 0)
 - nips.vols           -- volume information, 1 line per document (line 1 = volume 0 
                          = year 1987, one volume per year continuously. Note: publication year
                          officially is one year later, as NIPS is held in December)
 - nips.cite          -- citation information, 1 line per document, (docid )*
 - nips.ment           -- mentioning authors that are in the NIPS community don't need direct
                          citations. format like citations, but indexing author ids. Does not
                          contain the authors of the document but may overlap with citations.
 - documents.txt       -- citations of the documents (publication dates starting 1987)
                              
Some statistics on the data set (with references to the respective files in brackets):

 - M = 1740 documents [docs] from Y = 13 years/volumes [vols], with
 - M_L = 1254 labeled documents [labels] with L = 50 different category labels [labels.key] (one per document, some synonymous [labels.extract]), 
 - V = 13649 unique terms [vocab] instatiated by W=2301375 words [corpus], 
 - A = 2037 authors [authors.key] with W_A = 3990 authorship relations [authors], 
 - C = 1287 internal citations [cite] in M_C = 815 documents [cite], citing M_D = 591 documents [cite], and  
 - E = 21153 "author mentions" [ment].
    
 


