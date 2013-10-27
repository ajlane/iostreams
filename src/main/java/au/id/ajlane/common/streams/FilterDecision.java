/*
 * Copyright 2013 Aaron Lane
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

package au.id.ajlane.common.streams;

/**
 * Represents a filtering decision made by a {@link StreamFilter}.
 * <p/>
 * Filter decisions, especially early termination decisions, are advisory only. The decisions of several filters may be
 * combined to form the final filter decision.
 *
 * @see StreamFilter
 * @see Streams#filter
 */
public enum FilterDecision
{
    /**
     * Use the current item in the {@code Stream} and continue iterating.
     */
    KEEP_AND_CONTINUE,
    /**
     * Don't use the current item in the {@code Stream}, but keep iterating over future items.
     */
    SKIP_AND_CONTINUE,
    /**
     * Keep the the current item, but do not bother to process any more. (terminate early).
     */
    KEEP_AND_TERMINATE,
    /**
     * Stop iterating over the {@code Stream} (terminate early). Do not keep the current item.
     */
    SKIP_AND_TERMINATE
}
