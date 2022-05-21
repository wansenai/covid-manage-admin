package com.summer.common.core;

import java.lang.reflect.Field;

public interface StringEnum {
	default  <T extends Enum<T>> void changeNameTo(T enumT, String value) {
		try {
			Field field = enumT.getClass().getSuperclass().getDeclaredField("name");
			field.setAccessible(true);
			field.set(enumT, value);
			field.setAccessible(false);
		} catch (Exception e) {
			throw new RuntimeException("enum " + enumT + "change name to " + value + "error...", e);
		}
	}
}
