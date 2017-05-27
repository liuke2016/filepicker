# filepicker
A file picker for Android,imitate WeChat's picture selection module.


### Download
Grab via Maven:
```xml
<dependency>
  <groupId>com.squareup.okhttp3</groupId>
  <artifactId>okhttp</artifactId>
  <version>3.8.0</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.lynn.filepicker:filepicker:1.0.1'
```
###Usage
##PickImage
    ```java
   FilePicker.pickImage(this).subscribe(new Consumer<ArrayList<ImageFile>>() {
       @Override
       public void accept(ArrayList<ImageFile> imageFiles) throws Exception {
          //
       }
    });
    
    ```

### License
    Copyright (C) 2017 Lynn

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
