package org.cloudgraph.web.etl.loader;

import java.io.File;

public interface Loader {
	public void load(File file);
	public void define(File queryFile);
}
