package helpers;

import helpers.CEAFType;

public enum MatType {
	ABSOLUTE, RELATIVE;
	public static MatType matchingType (CEAFType t) {
		return t==CEAFType.MENTION ? ABSOLUTE : RELATIVE;
	}
}
