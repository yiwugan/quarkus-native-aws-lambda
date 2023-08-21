FROM  public.ecr.aws/lambda/provided

ADD target/quarkus-native-lambda-example-1.0-runner /var/runtime/bootstrap
RUN chmod ugo+x /var/runtime/bootstrap

CMD ["ggan.example.LambdaRotateEcrPassword::handleRequest"]
