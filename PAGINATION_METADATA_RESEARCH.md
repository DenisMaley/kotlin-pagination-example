# Pagination metadata

## HTTP Response Headers

It can be added as HTTP headers to the response, while leaving the actual response data untouched:

```json
[
    {"id": "product-1"},
    {"id": "product-2"}
]
```

## Response Envelope

However, many APIs out there (such as Facebook or Twitter) prefer to wrap the response in a 
data field of an envelope object, and add other meta data to that wrapper:

```json
{
    "_meta": {
        "nextPage": "/productss?/products?limit=20&after_id=20&sort_by=title"
    },
    "data": [
        {"id": "product-1"},
        {"id": "product-2"}
    ]
}
```

It's even touted as the way to go on the following website that 
proposes a standard for JSON APIs: https://jsonapi.org/

### Downsides
* The meta data was not really needed by the client apps after all.
They use infini-scrollers and simply request the next page until the API responds with an empty result set.
Also total amount of result items or amount of pages is basically ignored. 
We didn't need to update the UI with that information, and we did not implement an old-school navigation bar.

* Response data is a bit awkward to handle in JavaScript code:
Sure, this is really just a cosmetic thing that can be easily fixed by renaming the data property to 
something else such as `results` or `records`. 
But that would also mean breaking compatibility with that JSON:API standard I mentioned earlier ;)

```
axios.get('/products').then(response => {
    //                              +-- here I mean
    //                              v
    let resultList = response.data.data; 
    // ...
});
```

* It repeatedly introduced the same kind of bug in the client apps. 
The extra jump to the actual result set response.data.data was often overlooked, 
which repeatedly caused bugs that were only detected while testing. 
And some of them remained hidden for a while.
  
* The added confusion probably also stemmed from the fact that individual resources were not 
wrapped in an envelope and therefor directly accessible through response.data.

* HTTP is already the [envelope](https://stackoverflow.com/questions/9989135/when-in-my-rest-api-should-i-use-an-envelope-if-i-use-it-in-one-place-should-i/9999335#9999335). 
There really shouldn't be a need for wrapping result sets in an envelope data structure.