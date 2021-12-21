package gmail.maihai86.exam.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

/**
 * The persistent class for the user database table.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = 65981149772133526L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "USER_ID")
    private Long id;

    @Column(name = "PROVIDER_USER_ID")
    private String providerUserId;

    @Column(name = "EMAIL")
    private String email;

    @Column(name = "ENABLED")
    private boolean enabled = false;

    @Column(name = "DISPLAY_NAME")
    private String displayName;

    @Column(name = "CREATED_DATE", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    protected Date createdDate;

    @Column(name = "MODIFIED_DATE")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date modifiedDate;

    @Column(name = "PASSWORD")
    private String password;

    @Column(name = "PROVIDER")
    private String provider;

    // bi-directional many-to-many association to Role
    @JsonIgnore
    @ManyToMany
    @JoinTable(name = "user_role", joinColumns = {@JoinColumn(name = "USER_ID")}, inverseJoinColumns = {@JoinColumn(name = "ROLE_ID")})
    private Set<Role> roles;

}