<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <title>Avalara</title>
    <link rel="stylesheet" type="text/css" href="/css/ava.css">
</head>

<body>

    <header class="header">
        <div class="bg-img"></div>
        <img src="/img/avalara.svg" class="log" alt="Avalara"/>

        <div class="social-icons"><a href="#request-demo"><i class="icomoon icon-mail"></i></a></div>

        </div>
        <a class="see-more" href="#tile-section"><i class="icomoon icon-chevron-with-circle-down"></i></a>
    </header>

    <div id="app" class="center">
        <div class="upload">
            <select v-model="year">
                <option>2017</option>
                <option>2012</option>
            </select>
            <input style="padding-bottom:10px;" type="file" id="file" ref="file" v-on:change="handleFileUpload()" />
            <br/>
            <button style="padding:5px" v-on:click="submitFile()">Submit</button>
        </div>
        <div class="timetable">
            <span class="msg" ></span>
            <table width="100%">
                    <thead>
                      <tr class="tdgray">
                        <th colspan="2">Summary</th>
                      </tr>
                      <tr v-for="sum in summary" class="altleft">
                        <th>{{sum.row}}</th>
                        <th>{{sum.message}}</th>
                      </tr>
                      <tr style="background:gold">
                        <th colspan="2">{{msg}}</th>
                      </tr>
                      <tr class="tdgray">
                        <th>Row ID</th>
                        <th>Validation failure message</th>
                      </tr>
                    </thead>
                    <tbody>
                        <tr v-for="err in errors">
                            <td>{{err.row}}</td>
                            <td>{{err.message}}</td>
                        </tr>
                    </tbody>
            </table>
        </div>
    </div>

    <script src="/js/axios.min.js"></script>
    <script src="/js/vue.js"></script>
    <script>
    var app = new Vue({
      el: "#app",
      data: {
        file: '',
        year: '2017',
        uploadPercentage: 0,
        msg: '',
        errors: [],
        summary: []
      },
      methods: {
        submitFile() {
          let formData = new FormData();
          formData.append('uploaded_file', this.file);
          console.log('>> formData >> ', formData);

          // You should have a server side REST API
          var that = this;
          var validateUrl = '/validate?year='+this.year;
          axios.post(validateUrl,
              formData, {
                headers: {
                  'Content-Type': 'multipart/form-data'
                },
                onUploadProgress: function(pe) {
                    that.uploadPercentage = parseInt( Math.round( ( pe.loaded / pe.total ) * 100 ));
                    if(that.uploadPercentage == 100){
                        that.msg = "Upload Complete. Running Validation.";
                    }
                }
               }
            ).then(function (response) {
              console.log(response.data);
              if(response.data.success)
              {
                that.msg = "All Ok! No errors found";
              }else {
                that.msg = "Found the following errors.";
                that.errors.splice(0,that.errors.length);
                console.log(response.data.errors);
                that.errors.push.apply(that.errors,response.data.data.errors);
                //that.errors.push.apply(response.data.errors,that.errors);
              }
              that.summary.splice(0,that.summary.length);
              that.summary.push.apply(that.summary,response.data.data.summary);
            })
            .catch(function (error) {
              console.log(error);
            });
        },
        handleFileUpload() {
          this.errors.splice(0,this.errors.length);
          this.summary.splice(0,this.summary.length);
          this.file = this.$refs.file.files[0];
          console.log('>>>> 1st element in files array >>>> ', this.file);
        }
      }
    });
    </script>

</body>
</html>