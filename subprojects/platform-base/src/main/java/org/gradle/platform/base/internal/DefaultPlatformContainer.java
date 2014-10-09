/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.platform.base.internal;

import org.gradle.api.Action;
import org.gradle.api.internal.DefaultPolymorphicDomainObjectContainer;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.platform.base.Platform;
import org.gradle.platform.base.PlatformContainer;

import java.util.ArrayList;
import java.util.List;

public class DefaultPlatformContainer extends DefaultPolymorphicDomainObjectContainer<Platform> implements PlatformContainer {

    private List<Platform> searchOrder = new ArrayList<Platform>();

    public DefaultPlatformContainer(Class<? extends Platform> type, Instantiator instantiator) {
        super(type, instantiator);
        whenObjectAdded(new Action<Platform>() {
            public void execute(Platform platform) {
                searchOrder.add(platform);
            }
        });
        whenObjectRemoved(new Action<Platform>() {
            public void execute(Platform platform) {
                searchOrder.remove(platform);
            }
        });
    }

    public <T extends Platform> List<T> select(Class<T> type, List<String> targets) {
        T defaultElement = null;
        for (Platform platform : searchOrder) {
            if (type.isInstance(platform)) {
                defaultElement = type.cast(platform);
                break;
            }
        }
        return new NamedElementSelector<T>(type, targets, defaultElement).transform(this);
    }

}