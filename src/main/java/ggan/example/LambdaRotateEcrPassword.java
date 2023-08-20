package ggan.example;

import java.util.Base64;
import java.util.List;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SecretsManagerRotationEvent;
import jakarta.enterprise.context.ApplicationScoped;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ecr.EcrClient;
import software.amazon.awssdk.services.ecr.model.AuthorizationData;
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenResponse;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.utils.StringUtils;

// Handler value: example.HandlerInteger
/*
 * ggan 202308
 */
@ApplicationScoped
public class LambdaRotateEcrPassword implements RequestHandler<SecretsManagerRotationEvent, Void> {
//	private static String ECR_SECRET_ID = "arn:aws:secretsmanager:ca-central-1:662830732675:secret:nonprod/ecr/password-Tqh8yK";

	/*
	 * Takes in an InputRecord, which contains two integers and a String. Logs the
	 * String, then returns the sum of the two Integers.
	 */
	@Override
	public Void handleRequest(SecretsManagerRotationEvent event, Context context) {
		System.out.println("Received event:"+event);
		String ecrSecretId=event.getSecretId();
		// validate
		if (StringUtils.isBlank(ecrSecretId)) {
			System.out.println("handleRequest exit, secretId is null or emoty");
			return null;
		}
		
		//assume role
		System.out.println("Assume role");
		
		// get new password
		System.out.println("Calling ECR to get new login password");
		EcrClient ecrClient = EcrClient.builder()
				.region(Region.CA_CENTRAL_1)
				.build();
		GetAuthorizationTokenResponse ecrTokenResponse=ecrClient.getAuthorizationToken();

		List<AuthorizationData> authorizationData = ecrTokenResponse.authorizationData();
		String encodedToken = authorizationData.get(0).authorizationToken();
		String ecrPassword = new String(Base64.getDecoder().decode(encodedToken)).split(":")[1];
		System.out.println("Received ECR login password");
		ecrClient.close();
//		System.out.println("----------------------------");
//		System.out.println(encodedToken);
//		System.out.println("----------------------------");
//		System.out.println(ecrPassword);
//		System.out.println("----------------------------");
		
		System.out.println("Calling SecretsManager to update ECR password");
		SecretsManagerClient secretsClient = SecretsManagerClient.builder()
				.region(Region.CA_CENTRAL_1)
				.build();
		PutSecretValueRequest putRequest = PutSecretValueRequest.builder()
				.secretId(ecrSecretId)
				.secretString(ecrPassword)
				.build();
		secretsClient.putSecretValue(putRequest);
		secretsClient.close();
		
		System.out.println("handleRequest completed");
		return null;

	}
	
}
