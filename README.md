[![CircleCI](https://circleci.com/gh/jcpsantiago/controltower/tree/master.svg?style=svg)](https://circleci.com/gh/jcpsantiago/controltower/tree/master)

# clj-googledrive-form

Creating a form to upload files to Google Drive was useful for a project at work,
so here's a version of that for posteriority.

I followed the usual procedure to create an OAuth token for a webapp, but then
used [OAuth Playground](https://developers.google.com/oauthplayground/) to get
a refresh token by using my app's client-id and secret. Finally, took that
refresh token and used for the authentication process. All the calls to Google
Drive are done using the REST API interface.

Another neat thing is the integration with Slack through a webhook, so I get notified
whenever someone uploads a new file.

I did the frontend using [Tachyons CSS](http://tachyons.io/) which was a delight,
once I discovered [Tachyons TL;DR](https://tachyons-tldr.now.sh).

If you feel like sending a spreadsheet to a stranger's Google Drive folder,
here's this app deployed: [https://clj-googledrive-form.herokuapp.com/](https://clj-googledrive-form.herokuapp.com/)

<p align="center">
  <img width="320" height="568" src="https://shrinktheweb.snapito.io/v2/webshot/spu-ea68c8-ogi2-3cwn3bmfojjlb56e?size=640x1136&screen=640x1136&url=https%3A%2F%2Fclj-googledrive-form.herokuapp.com">
</p>
