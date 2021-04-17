/*
 * Copyright (C) 2020 ODK
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package app.nexusforms.android.database;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.runner.RunWith;

import app.nexusforms.android.instances.InstancesRepository;
import app.nexusforms.android.instances.InstancesRepositoryTest;
import app.nexusforms.android.storage.StorageInitializer;
import app.nexusforms.android.support.RobolectricHelpers;

@RunWith(AndroidJUnit4.class)
public class DatabaseInstancesRepositoryTest extends InstancesRepositoryTest {
    @Before
    public void setup() {
        RobolectricHelpers.mountExternalStorage();
        new StorageInitializer().createOdkDirsOnStorage();
    }

    @Override
    public InstancesRepository buildSubject() {
        return new DatabaseInstancesRepository();
    }
}
