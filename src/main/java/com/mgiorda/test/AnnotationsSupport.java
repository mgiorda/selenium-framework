package com.mgiorda.test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mgiorda.annotations.By;
import com.mgiorda.annotations.Locate;
import com.mgiorda.test.ProtectedPageClasses.AbstractElement;
import com.mgiorda.test.ProtectedPageClasses.Locator;
import com.mgiorda.test.ProtectedPageClasses.PageElement;

class AnnotationsSupport {

	private static final Log logger = LogFactory.getLog(AnnotationsSupport.class);

	private AnnotationsSupport() {

	}

	public static <T extends AbstractPage> void initLocators(T page) {

		Class<?> pageClass = page.getClass();

		Field[] declaredFields = pageClass.getDeclaredFields();
		for (Field field : declaredFields) {

			Locate annotation = field.getAnnotation(Locate.class);
			if (annotation != null) {

				Locator[] locators = getLocatorsFromAnnotation(annotation);
				if (locators.length == 0) {
					throw new IllegalStateException(String.format("Couldn't find an element locator for field '%s' in page class '%s'", field.getName(), pageClass.getSimpleName()));
				}

				Object value = getLocatorElement(field.getType(), page.getElementHandler(), locators);
				if (value == null) {
					throw new IllegalStateException(String.format("Cannot autowire field '%s' of type '%s' in page '%s'", field.getName(), field.getType(), pageClass));
				}

				setField(page, field, value);
			}
		}
	}

	private static Locator[] getLocatorsFromAnnotation(Locate annotation) {

		List<Locator> locators = new ArrayList<Locator>();
		By[] multipleLocators = annotation.value();

		for (By byAnnotation : multipleLocators) {
			Locator locator = getByAnnotation(byAnnotation);
			if (locator != null) {
				locators.add(locator);
			}
		}

		Locator[] locatorsArray = locators.toArray(new Locator[] {});

		return locatorsArray;
	}

	private static Locator getByAnnotation(By annotation) {

		Locator elementLocator = null;

		String id = annotation.id();
		String name = annotation.name();
		String css = annotation.css();
		String className = annotation.className();
		String tagName = annotation.tagName();
		String linkText = annotation.linkText();
		String partialLinkText = annotation.partialLinkText();
		String xpath = annotation.xpath();

		if (!id.equals("")) {
			elementLocator = Locator.byId(id);
		} else if (!name.equals("")) {
			elementLocator = Locator.byName(name);
		} else if (!css.equals("")) {
			elementLocator = Locator.byCssSelector(css);
		} else if (!className.equals("")) {
			elementLocator = Locator.byClass(className);
		} else if (!tagName.equals("")) {
			elementLocator = Locator.byTagName(tagName);
		} else if (!linkText.equals("")) {
			elementLocator = Locator.byLinkText(linkText);
		} else if (!partialLinkText.equals("")) {
			elementLocator = Locator.byPartialLinkText(partialLinkText);
		} else if (!xpath.equals("")) {
			elementLocator = Locator.byXpath(xpath);
		}

		return elementLocator;
	}

	private static Object getLocatorElement(Class<?> fieldType, PageElementHandler pageElementHandler, Locator[] locators) {

		Object value = null;

		if (fieldType.isAssignableFrom(Boolean.class) || fieldType.isAssignableFrom(boolean.class)) {
			value = pageElementHandler.existsElement(locators);

		} else if (fieldType.isAssignableFrom(Integer.class) || fieldType.isAssignableFrom(int.class)) {
			value = pageElementHandler.getElementCount(locators);

		} else if (fieldType.isAssignableFrom(PageElement.class)) {
			value = pageElementHandler.getElement(locators);

		} else if (fieldType.isAssignableFrom(List.class)) {
			value = pageElementHandler.getElements(locators);

		} else if (fieldType.isAssignableFrom(AbstractElement.class)) {

			@SuppressWarnings("unchecked")
			Class<? extends AbstractElement> elementClass = (Class<? extends AbstractElement>) fieldType;
			value = getValueForAbstractElement(elementClass, pageElementHandler, locators);
		}

		return value;
	}

	private static Object getValueForAbstractElement(Class<? extends AbstractElement> fieldType, PageElementHandler pageElementHandler, Locator[] locators) {

		PageElement pageElement = pageElementHandler.getElement(locators);
		Object element = AbstractElement.factory(fieldType, pageElementHandler, pageElement);

		return element;
	}

	private static <T extends AbstractPage> void setField(T page, Field field, Object value) {

		boolean isFieldAccessible = field.isAccessible();

		field.setAccessible(true);
		try {

			logger.info(String.format("Setting '%s' page field '%s' with element '%s'", page.getClass().getSimpleName(), field.getName(), value));
			field.set(page, value);

		} catch (IllegalArgumentException | IllegalAccessException e) {

			throw new IllegalStateException(e);
		}
		field.setAccessible(isFieldAccessible);
	}
}
