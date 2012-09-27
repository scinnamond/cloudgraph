package org.cloudgraph.web.data.loader;

import java.io.File;

public interface Loader {
	public void load(File file);
	public void define(File queryFile);
}
