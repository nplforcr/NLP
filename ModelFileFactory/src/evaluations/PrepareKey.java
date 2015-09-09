package evaluations;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import utils.CorefClusters;

public interface PrepareKey {

	public List<CorefClusters> getKeyData(File conFile,File chainFile,File docFile) throws IOException;
	public List<CorefClusters> getKeyData(File conFile,File chainFile) throws IOException;
	public ArrayList<CorefClusters> getKeyData(List<String> bsTokenList);
}
