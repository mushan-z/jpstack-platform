package wbs.framework.builder;

import java.util.List;

public
interface Builder {

	void descend (
		Object parentObject,
		List<?> childObjects,
		Object targetObject,
		MissingBuilderBehaviour missingBuilderBehaviour);

	public
	enum MissingBuilderBehaviour {
		ignore,
		error;
	}

}
