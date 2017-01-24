/*
 * Copyright 2000-2016 Vaadin Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.data.provider;

import java.util.Collections;
import java.util.List;

import com.vaadin.server.SerializableBiFunction;

/**
 * A data provider that lazy loads items from a back end.
 *
 * @param <T>
 *            data provider data type
 * @param <F>
 *            data provider filter type
 */
public interface BackEndDataProvider<T, F> extends DataProvider<T, F> {

    /**
     * Sets a list of sort orders to use as the default sorting for this data
     * provider. This overrides the sorting set by any other method that
     * manipulates the default sorting of this data provider.
     * <p>
     * The default sorting is used if the query defines no sorting. The default
     * sorting is also used to determine the ordering of items that are
     * considered equal by the sorting defined in the query.
     *
     * @see #setSortOrder(SortOrder)
     *
     * @param sortOrders
     *            a list of sort orders to set, not <code>null</code>
     */
    void setSortOrders(List<SortOrder<String>> sortOrders);

    /**
     * Sets a single sort order to use as the default sorting for this data
     * provider. This overrides the sorting set by any other method that
     * manipulates the default sorting of this data provider.
     * <p>
     * The default sorting is used if the query defines no sorting. The default
     * sorting is also used to determine the ordering of items that are
     * considered equal by the sorting defined in the query.
     *
     * @see #setSortOrders(List)
     *
     * @param sortOrder
     *            a sort order to set, or <code>null</code> to clear any
     *            previously set sort orders
     */
    default void setSortOrder(SortOrder<String> sortOrder) {
        if (sortOrder == null) {
            setSortOrders(Collections.emptyList());
        } else {
            setSortOrders(Collections.singletonList(sortOrder));
        }
    }

    @Override
    default boolean isInMemory() {
        return false;
    }

    /**
     * Wraps this data provider to create a data provider that supports
     * programmatically setting a filter that will be combined with a filter
     * provided through the query.
     *
     * @see #withConfigurableFilter()
     *
     * @param filterCombiner
     *            a callback for combining and the configured filter with the
     *            filter from the query to get a filter to pass to the wrapped
     *            provider. Will only be called if the query contains a filter.
     *
     * @return a data provider with a configurable filter, not <code>null</code>
     */
    public default <C> ConfigurableFilterDataProvider<T, C, F> withConfigurableFilter(
            SerializableBiFunction<F, C, F> filterCombiner) {
        return new ConfigurableFilterDataProviderWrapper<T, C, F>(this) {
            @Override
            protected F combineFilters(F configuredFilter, C queryFilter) {
                return filterCombiner.apply(configuredFilter, queryFilter);
            }
        };
    }

    /**
     * Wraps this data provider to create a data provider that supports
     * programmatically setting a filter but no filtering through the query.
     *
     * @see #withConfigurableFilter(SerializableBiFunction)
     *
     * @return a data provider with a configurable filter, not <code>null</code>
     */
    public default ConfigurableFilterDataProvider<T, Void, F> withConfigurableFilter() {
        return withConfigurableFilter((configuredFilter, queryFilter) -> {
            assert queryFilter == null : "Filter from Void query must be null";

            return configuredFilter;
        });
    }
}