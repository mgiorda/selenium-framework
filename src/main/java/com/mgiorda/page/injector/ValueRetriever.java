package com.mgiorda.page.injector;

import java.lang.reflect.Field;

import com.mgiorda.page.Locator;

public interface ValueRetriever {

    Object getValueForLocators(Field field, Locator[] locators);
}
