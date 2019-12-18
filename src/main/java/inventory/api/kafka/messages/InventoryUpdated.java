/*
 *   Copyright 2019 IBM Corporation.
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package inventory.api.kafka.messages;

public class InventoryUpdated {
    private long id;
    private long decrementAmount;

    public InventoryUpdated() {
    }

    public InventoryUpdated(long id, long decrementAmount) {
        this.id = id;
        this.decrementAmount = decrementAmount;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getDecrementAmount() {
        return decrementAmount;
    }

    public void setDecrementAmount(long decrementAmount) {
        this.decrementAmount = decrementAmount;
    }
}
