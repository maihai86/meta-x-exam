package gmail.maihai86.exam.dto;

import lombok.Getter;

/**
 * @author maihai86@gmail.com
 */
@Getter
public enum SocialProvider {

    FACEBOOK("facebook"), GOOGLE("google"), LOCAL("local");

    private final String providerType;

    SocialProvider(final String providerType) {
        this.providerType = providerType;
    }

}
