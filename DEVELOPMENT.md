
To build sign and deploy to Sonatype OSS (Maven Central):

You will need to enter the passphrase for your private GPG key at some point.
Please do not forget to distribute it to hkp://pgp.mit.edu if the signed
bundle is to be accepted in Maven Central!

```shell
mvn -P sonatype-deploy clean deploy -Dgpg.passphrase=X
```
where X=your gpg password


Check it, then release it

```shell
mvn -P sonatype-deploy nexus-staging:release
```
