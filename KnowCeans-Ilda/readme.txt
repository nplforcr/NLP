Implementations of Latent Dirichlet Allocation (LDA) and
Hierarchical Dirichlet Processes (HDP)

@author Gregor Heinrich, gregor :: arbylon : net
@version 0.96
@date 1 Mar 2011 

 - History: ILDA version 0.1: May 2008, LDA version 0.1: Feb. 2005, based
   on http://arbylon.net/projects/LdaGibbsSampler.java  

 - Simple implementations of Gibbs sampling for LDA and HDP
 
 - Scientific documentation: see texts lda.pdf and ilda.pdf
 
 - Technical documentation: see Javadoc and source (packages *.corpus and 
   *.utils are from knowceans-tools on SourceForge)
 
 - Data documentation: see nips/readme.txt including source references
 
 - License: All code is licensed under GPL v3.0. 
 
 - If the code is used in scientific work, please refer to its source
   via the URL: 
   
   http://arbylon.net/projects/knowceans-ilda.zip
   	
   or the documentation of the ILDA or LDA implementations:
   
   G. Heinrich. "Infinite LDA" -- implementing the HDP with minimum code
   complexity. TN2011/1, http://arbylon.net/publications/ilda.pdf, 2011
   
   G. Heinrich. Parameter estimation for text analysis. Technical report,
   No. 09RP008-FIGD, Fraunhofer IGD, 2009 
 
TODO:

 - Diverse checks, e.g., Antoniak distribution sampling, hyperparameter
   estimators, general quantitative validation of HDP model

 - Output formatting
 
 - Visual matrix implementation for HDP / IldaGibbs
 