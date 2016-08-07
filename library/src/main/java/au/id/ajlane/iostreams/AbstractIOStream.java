/*
 * Copyright 2016 Aaron Lane
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

package au.id.ajlane.iostreams;

public abstract class AbstractIOStream<T> implements IOStream<T>
{
    @Override
    public final void consume(final IOStreamConsumer<? super T> consumer) throws IOStreamException
    {
        try(IOStreamConsumer<? super T> autoCloseConsumer = consumer)
        {
            generate(consumer);
        }
        catch (RuntimeException ex){
            throw ex;
        }
        catch (Exception ex)
        {
            throw new IOStreamException(ex);
        }
    }

    protected abstract void generate(final IOStreamConsumer<? super T> consumer) throws Exception;
}
