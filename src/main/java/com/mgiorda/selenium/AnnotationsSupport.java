package com.mgiorda.selenium;

import java.lang.reflect.Field;

import org.openqa.selenium.By;
import org.openqa.selenium.support.FindBy;

import com.mgiorda.selenium.AbstractPage.PageElement;

class AnnotationsSupport {

	private AnnotationsSupport() {

	}

	public static <T extends AbstractPage> void initFindBy(T page) {

		Class<?> pageClass = page.getClass();

		Field[] declaredFields = pageClass.getDeclaredFields();
		for (Field field : declaredFields) {

			FindBy annotation = field.getAnnotation(FindBy.class);
			if (annotation != null && field.getType().isAssignableFrom(PageElement.class)) {

				By elementLocator = getByFromAnnotation(annotation);
				if (elementLocator == null) {
					throw new IllegalStateException(String.format("Couldn't find an element locator for field '%s' in page class '%s'", field.getName(), pageClass.getSimpleName()));
				}

				setFieldBy(page, field, elementLocator);
			}
		}
	}

	private static By getByFromAnnotation(FindBy annotation) {

		By elementLocator = null;

		String id = annotation.id();
		String name = annotation.name();
		String css = annotation.css();
		String className = annotation.className();
		String tagName = annotation.tagName();
		String linkText = annotation.linkText();
		String partialLinkText = annotation.partialLinkText();
		String xpath = annotation.xpath();

		if (!id.equals("")) {
			elementLocator = By.id(id);
		} else if (!name.equals("")) {
			elementLocator = By.name(name);
		} else if (!css.equals("")) {
			elementLocator = By.cssSelector(css);
		} else if (!className.equals("")) {
			elementLocator = By.className(className);
		} else if (!tagName.equals("")) {
			elementLocator = By.tagName(tagName);
		} else if (!linkText.equals("")) {
			elementLocator = By.linkText(linkText);
		} else if (!partialLinkText.equals("")) {
			elementLocator = By.partialLinkText(partialLinkText);
		} else if (!xpath.equals("")) {
			elementLocator = By.xpath(xpath);
		}

		return elementLocator;
	}

	private static <T extends AbstractPage> void setFieldBy(T page, Field field, By elementLocator) {

		PageElement element = page.getElement(elementLocator);

		boolean isFieldAccessible = field.isAccessible();

		field.setAccessible(true);
		try {
			field.set(page, element);
		} catch (IllegalArgumentException | IllegalAccessException e) {

			throw new IllegalStateException(e);
		}
		field.setAccessible(isFieldAccessible);
	}
}
