# jersey-sample
An example Jersey application that computes term frequencies and classifies text. The classifier implementation computes a document centroid by averaging the term frequencies across all documents in a specific genre. It could probably be improved by removing stop words, [stemming the words][stem], and possibly weighting the terms (e.g. using [TF-IDF][tf-idf]). I could have used Lucene for some of this too but wanted to give it a shot myself.

[tf-idf]: https://monkeylearn.com/blog/what-is-tf-idf/#:~:text=TF%2DIDF%20is%20a%20statistical,across%20a%20set%20of%20documents.
[stem]: https://nlp.stanford.edu/IR-book/html/htmledition/stemming-and-lemmatization-1.html

# Running locally
You can run with Gradle:
```sh
./gradlew run --info
```

Or with Docker:
```sh
# build the Docker image
./gradlew jibDockerBuild --info

# start a container
docker run -p 8080:8080 -p 9010:9010 -ti mitchseymour/jetty-sample:0.1.0
```

I have also added a JMX Prometheus exporter for the Docker build. So if running the Docker container, you can view metrics on port 9010:
```sh
curl localhost:9010
```

# Usage

## Get term frequencies
```sh
# example request
curl -G 'localhost:8080/termFrequencies' \
    --data-urlencode "documentText=The story unfolds in Melbourne, Australia with two spoilt rich brothers and best friends Robby (Amrinder Gill), Rolly (Honey Singh). Their businessman father is worried about the future of his irresponsible sons so he strikes a deal with them, by throwing the brats out of his house sending them to Punjab so that they can understand the realities of life and importance of their roots and heritage. The film is about how these two spoilt brothers arrive in Punjab and learn to live with the struggle, whilst being challenged by their father to come up with Rs.30 lakhs in 30 days in order to inherit his wealth.Otherwise the wealth would be transferred to charity which would be maintained by their father's secretary."

# example response
{"transferred":1,"brothers":2,"wealthotherwise":1,"about":2,"these":1,"that":1,"his":3,"would":2,"sons":1,"charity":1,"amrinder":1,"up":1,"maintained":1,"businessman":1,"melbourne":1,"struggle":1,"they":1,"order":1,"which":1,"punjab":2,"spoilt":2,"in":4,"robby":1,"unfolds":1,"understand":1,"come":1,"heritage":1,"them":2,"is":2,"australia":1,"being":1,"film":1,"roots":1,"friends":1,"realities":1,"singh":1,"lakhs":1,"fathers":1,"deal":1,"be":2,"importance":1,"father":2,"their":4,"brats":1,"best":1,"house":1,"two":2,"life":1,"out":1,"throwing":1,"how":1,"can":1,"gill":1,"secretary":1,"challenged":1,"arrive":1,"honey":1,"and":4,"by":3,"strikes":1,"irresponsible":1,"of":4,"sending":1,"worried":1,"so":2,"30":1,"live":1,"wealth":1,"a":1,"learn":1,"rolly":1,"rich":1,"rs30":1,"whilst":1,"the":7,"with":4,"future":1,"inherit":1,"days":1,"to":5,"he":1,"story":1}
```

## Get similarity score

### Identical docs
Getting the similarity between two identical documents will return a similarity of `1.0`.
```sh
# example request
curl -G 'localhost:8080/similarityScore' \
    --data-urlencode "documentText1=The story unfolds in Melbourne, Australia with two spoilt rich brothers." \
    --data-urlencode "documentText2=The story unfolds in Melbourne, Australia with two spoilt rich brothers."

# example response
1.0
```

### Dissimilar docs
Getting the similarity between two different documents will return a similarity of < `1.0`.
```sh
# example request
curl -G 'localhost:8080/similarityScore' \
    --data-urlencode "documentText1=The story unfolds in Melbourne, Australia with two spoilt rich brothers." \
    --data-urlencode "documentText2=The story unfolds in a dystopian future with a smoldering space craft"

# example response
0.4029114820126901
```

## Save documents to a genre
```sh
# example requests
curl -XPUT -G 'localhost:8080/genreDocument' \
    --data-urlencode "genre=music" \
    --data-urlencode "docId=123" \
    --data-urlencode "documentText=Synthwave is an electronic music microgenre that is based predominately on the music associated with action, science-fiction, and horror film soundtracks of the 1980s"

curl -XPUT -G 'localhost:8080/genreDocument' \
    --data-urlencode "genre=film" \
    --data-urlencode "docId=456" \
    --data-urlencode "documentText=A horror film is one that seeks to elicit fear in its audience for entertainment purposes"

curl -XPUT -G 'localhost:8080/genreDocument' \
    --data-urlencode "genre=film" \
    --data-urlencode "docId=789" \
    --data-urlencode "documentText=Thriller film, also known as suspense film or suspense thriller, is a broad film genre that evokes excitement and suspense in the audience"
```

## Get documents in a genre
```sh
# example requests
curl -XGET -G 'localhost:8080/genreDocuments' \
    --data-urlencode "genre=film"

# example response
["456","789"]
```

## Remove documents from a genre
```sh
curl -XDELETE -G 'localhost:8080/genreDocument' \
    --data-urlencode "genre=film" \
    --data-urlencode "docId=456"

# verify (doc ID 456 should no longer appear in output)
curl -XGET -G 'localhost:8080/genreDocuments' \
    --data-urlencode "genre=film"

["789"]
```

## Get closest genres
```sh
# example request
curl -XGET -G 'localhost:8080/nClosestGenres' \
    --data-urlencode "count=3" \
    --data-urlencode "documentText=listening to electronic helps me focus"

# example response
["music","film"]
```

You can also adjust the max genre count:
```sh
# example request
curl -XGET -G 'localhost:8080/nClosestGenres' \
    --data-urlencode "count=1" \
    --data-urlencode "documentText=listening to electronic helps me focus"

# example response
["music"]
```

# Notes
- I'm pretty confident in each call except the last (`nClosestGenres`). I tested on toy data but I don't think the document centroid approach is ideal as I test on larger document collections (the results aren't always as expected). If I had more time, I'd likely read more about text classification approaches and try something new. Or maybe delegate the work to some other library (e.g. Lucene)


