package SeCause.SeCause_be.domain.security.entity;

import SeCause.SeCause_be.domain.vulnerability.entity.CodeVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.InfraVulnerability;
import SeCause.SeCause_be.domain.vulnerability.entity.Vulnerability;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Getter
@Entity
@Table(name = "security_references")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SecurityReference {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "security_reference_id")
    private Long securityReferenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vulnerability_id", nullable = false)
    private Vulnerability vulnerability;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "reference_type", nullable = false, columnDefinition = "reference_type_enum")
    private ReferenceType referenceType;

    @Column(name = "title", length = 500)
    private String title;

    @Column(name = "reference_url", length = 1000)
    private String referenceUrl;

    private SecurityReference(
            Vulnerability vulnerability,
            ReferenceType referenceType,
            String title,
            String referenceUrl
    ) {
        this.vulnerability = vulnerability;
        this.referenceType = referenceType;
        this.title = title;
        this.referenceUrl = referenceUrl;
    }

    public static SecurityReference createForCodeVulnerability(
            CodeVulnerability codeVulnerability,
            ReferenceType referenceType,
            String title,
            String referenceUrl
    ) {
        return new SecurityReference(codeVulnerability, referenceType, title, referenceUrl);
    }

    public static SecurityReference createForInfraVulnerability(
            InfraVulnerability infraVulnerability,
            ReferenceType referenceType,
            String title,
            String referenceUrl
    ) {
        return new SecurityReference(infraVulnerability, referenceType, title, referenceUrl);
    }
}
