package com.fortify.processrunner.context;

import java.util.Collection;

public interface IContextGenerator {
	public Collection<Context> generateContexts(Context initialContext);
	public boolean isContextGeneratorEnabled();
}
