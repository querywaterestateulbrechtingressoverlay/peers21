package ru.cyphercola.peers21.datalayer.data;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("api_user_authorities")
public record ApiUserAuthority(
    @Id
    @Column("id")
    Integer id,
    @Column("api_user_login")
    String apiUserLogin,
    @Column("authority")
    String authority
) {
}
