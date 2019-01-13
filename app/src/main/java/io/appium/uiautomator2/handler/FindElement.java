/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.appium.uiautomator2.handler;

import android.support.annotation.Nullable;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.UUID;

import io.appium.uiautomator2.common.exceptions.ElementNotFoundException;
import io.appium.uiautomator2.common.exceptions.UiAutomator2Exception;
import io.appium.uiautomator2.common.exceptions.UiSelectorSyntaxException;
import io.appium.uiautomator2.handler.request.SafeRequestHandler;
import io.appium.uiautomator2.http.AppiumResponse;
import io.appium.uiautomator2.http.IHttpRequest;
import io.appium.uiautomator2.model.AndroidElement;
import io.appium.uiautomator2.model.By;
import io.appium.uiautomator2.model.By.ByClass;
import io.appium.uiautomator2.model.By.ById;
import io.appium.uiautomator2.model.KnownElements;
import io.appium.uiautomator2.model.internal.CustomUiDevice;
import io.appium.uiautomator2.model.internal.NativeAndroidBySelector;
import io.appium.uiautomator2.server.WDStatus;
import io.appium.uiautomator2.utils.Device;
import io.appium.uiautomator2.utils.ElementHelpers;
import io.appium.uiautomator2.utils.Logger;
import io.appium.uiautomator2.utils.NodeInfoList;

import static io.appium.uiautomator2.utils.AXWindowHelpers.refreshRootAXNode;
import static io.appium.uiautomator2.utils.Device.getAndroidElement;
import static io.appium.uiautomator2.utils.LocationHelpers.getXPathNodeMatch;
import static io.appium.uiautomator2.utils.LocationHelpers.rewriteIdLocator;
import static io.appium.uiautomator2.utils.LocationHelpers.toSelector;

public class FindElement extends SafeRequestHandler {

    public FindElement(String mappedUri) {
        super(mappedUri);
    }

    @Override
    protected AppiumResponse safeHandle(IHttpRequest request) throws JSONException,
            UiObjectNotFoundException {
        Logger.info("Find element command");
        KnownElements ke = new KnownElements();
        final JSONObject payload = getPayload(request);
        final String method = payload.getString("strategy");
        final String selector = payload.getString("selector");
        final String contextId = payload.getString("context");
        Logger.info(String.format("find element command using '%s' with selector '%s'.", method, selector));
        final By by = new NativeAndroidBySelector().pickFrom(method, selector);

        Device.waitForIdle();
        Object element;
        try {
            if (contextId.length() > 0) {
                element = this.findElement(by, contextId);
            } else {
                element = this.findElement(by);
            }
        } catch (ClassNotFoundException e) {
            throw new UiAutomator2Exception(e);
        }
        if (element == null) {
            return new AppiumResponse(getSessionId(request), WDStatus.NO_SUCH_ELEMENT);
        }

        String id = UUID.randomUUID().toString();
        AndroidElement androidElement = getAndroidElement(id, element, by);
        ke.add(androidElement);
        JSONObject result = ElementHelpers.toJSON(androidElement);
        return new AppiumResponse(getSessionId(request), WDStatus.SUCCESS, result);
    }

    @Nullable
    private Object findElement(By by) throws ClassNotFoundException, UiAutomator2Exception,
            UiObjectNotFoundException {
        refreshRootAXNode();

        if (by instanceof ById) {
            String locator = rewriteIdLocator((ById) by);
            return CustomUiDevice.getInstance().findObject(android.support.test.uiautomator.By.res(locator));
        } else if (by instanceof By.ByAccessibilityId) {
            return CustomUiDevice.getInstance().findObject(android.support.test.uiautomator.By.desc(by.getElementLocator()));
        } else if (by instanceof ByClass) {
            return CustomUiDevice.getInstance().findObject(android.support.test.uiautomator.By.clazz(by.getElementLocator()));
        } else if (by instanceof By.ByXPath) {
            final NodeInfoList matchedNodes = getXPathNodeMatch(by.getElementLocator(), null);
            if (matchedNodes.size() == 0) {
                throw new ElementNotFoundException();
            }
            return CustomUiDevice.getInstance().findObject(matchedNodes);
        } else if (by instanceof By.ByAndroidUiAutomator) {
            UiSelector selector = toSelector(by.getElementLocator());
            if (selector == null) {
                throw new UiSelectorSyntaxException(by.getElementLocator(), "");
            }
            return CustomUiDevice.getInstance().findObject(selector);
        }
        String msg = String.format("By locator %s is currently not supported!", by.getClass().getSimpleName());
        throw new UnsupportedOperationException(msg);
    }

    @Nullable
    private Object findElement(By by, String contextId) throws ClassNotFoundException,
            UiAutomator2Exception, UiObjectNotFoundException {
        AndroidElement element = KnownElements.getElementFromCache(contextId);
        if (element == null) {
            throw new ElementNotFoundException();
        }

        if (by instanceof ById) {
            String locator = rewriteIdLocator((ById) by);
            return element.getChild(android.support.test.uiautomator.By.res(locator));
        } else if (by instanceof By.ByAccessibilityId) {
            return element.getChild(android.support.test.uiautomator.By.desc(by.getElementLocator()));
        } else if (by instanceof ByClass) {
            return element.getChild(android.support.test.uiautomator.By.clazz(by.getElementLocator()));
        } else if (by instanceof By.ByXPath) {
            final NodeInfoList matchedNodes = getXPathNodeMatch(by.getElementLocator(), element);
            if (matchedNodes.size() == 0) {
                throw new ElementNotFoundException();
            }
            return CustomUiDevice.getInstance().findObject(matchedNodes);
        } else if (by instanceof By.ByAndroidUiAutomator) {
            UiSelector selector = toSelector(by.getElementLocator());
            if (selector == null) {
                throw new UiSelectorSyntaxException(by.getElementLocator(), "");
            }
            return element.getChild(selector);
        }
        String msg = String.format("By locator %s is currently not supported!", by.getClass().getSimpleName());
        throw new UnsupportedOperationException(msg);
    }
}
