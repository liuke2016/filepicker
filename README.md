# filepicker
A file picker for Android,imitate WeChat's picture selection module.


### Download
Grab via Maven:
```xml
<dependency>
  <groupId>com.lynn.filepicker</groupId>
  <artifactId>filepicker</artifactId>
  <version>1.0.1</version>
  <type>pom</type>
</dependency>
```
or Gradle:
```groovy
compile 'com.lynn.filepicker:filepicker:1.0.1'
```


## Usage
### PickImage
       FilePicker.pickImage(this)
       .subscribe(new Consumer<ArrayList<ImageFile>>() {
       @Override
       public void accept(ArrayList<ImageFile> imageFiles) throws Exception {
          //do something
       }
    });
### PickVideo
                FilePicker.pickVideo(this)
                        .subscribe(new Consumer<ArrayList<VideoFile>>() {
                            @Override
                            public void accept(ArrayList<VideoFile> videoFiles) throws Exception {
                               //do something
                            }
                        });
### PickAudio
                            FilePicker.pickAudio(this)
                        .subscribe(new Consumer<ArrayList<AudioFile>>() {
                            @Override
                            public void accept(ArrayList<AudioFile> imageFiles) throws Exception {
                                      //do something
                            }
                        });
### PickOtherFile
                FilePicker.pickOtherFile(this)
                        .subscribe(new Consumer<ArrayList<OtherFile>>() {
                            @Override
                            public void accept(ArrayList<OtherFile> otherFiles) throws Exception {
                                 //do something
                            }
                        });
                  
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
