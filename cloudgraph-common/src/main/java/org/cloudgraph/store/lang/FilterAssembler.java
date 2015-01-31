package org.cloudgraph.store.lang;

public interface FilterAssembler {

	public abstract String getFilter(); 
	public abstract Object[] getParams();
	public abstract String getVariableDeclarations();

	public abstract boolean hasVariableDeclarations();

	public abstract String getImportDeclarations();

	public abstract boolean hasImportDeclarations();

	public abstract String getParameterDeclarations();

	public abstract boolean hasParameterDeclarations();
}