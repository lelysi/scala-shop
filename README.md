# Scala shop

Shop example on scala/akka with travis-ci integration and deployment on heroku

## Getting Started

Project has no database, therefore, after restart it goes to initial state

It is up on https://scala-shop.herokuapp.com/ url

Current implementation is next endpoints (examples in [https://httpie.org/](httpie) style):

### Add items to shop
```
http POST https://scala-shop.herokuapp.com/add-shop-item price:=50 description="good one" count:=3
```
responds with new item UUID

### Create user
```
http POST https://scala-shop.herokuapp.com/registration email="example@example.com" password="coolpass" account="some data" 
```
responds with jwt key for the user

### Login
```
http POST https://scala-shop.herokuapp.com/login email="example@example.com" password="coolpass"
```
also responds with jwt key

### Add item to user cart
```
http POST https://scala-shop.herokuapp.com/add-item-to-cart \ 
X-Api-Key:"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.ZXhhbXBsZUBleGFtcGxlLmNvbQ.ACyRmboxoQ3LvT_ZNnDGKfd1rnGH3pu0XL_wk4uGMYQ" \
uuid=5c9a1da3-9d0c-4021-9449-84e82167797a
```
where jwt token should be provided as a value in X-Api-Key header
and uuid - is one of the UUID from "Add items to shop" request

### Checkout
```
http POST https://scala-shop.herokuapp.com/checkout \
X-Api-Key:"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.ZXhhbXBsZUBleGFtcGxlLmNvbQ.ACyRmboxoQ3LvT_ZNnDGKfd1rnGH3pu0XL_wk4uGMYQ"
```
responds with order UUID

## Installing

You can install it locally

```
git clone git@github.com:lelysi/scala-shop.git
sbt run
```

## Running the tests

To run test:

```
sbt test
```

All tests divided to unit and functional (API tests)

## Deployment

To deploy it needs to define next environment variables:
* HOST - default is localhost
* PORT - default is 8080
* SECRET - key for jwt

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Notes

* service has no database
* service has no frontend
* service has no get urls yet
* service uses travis-ci to run tests on each commit
* service has continuous delivery - after each commit to master it builds and deploys new version on https://scala-shop.herokuapp.com/ (discontinued)
