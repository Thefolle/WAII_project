package it.polito.waii.catalogue_service.entities


import it.polito.waii.catalogue_service.dtos.UserDTO
import org.hibernate.annotations.ColumnDefault
import javax.persistence.*

@Entity
@Table(indexes = [Index(name = "usernameIndex", columnList = "username", unique = true)])
class User(
    @Id
    @GeneratedValue
    var uid: Long?,
    @Column(unique = true, nullable = false)
    var username: String,
    var password: String,
    var name: String,
    var surname: String,
    @Column(unique = true, nullable = false)
    var email: String,
    var deliveryAddress: String,
    @ColumnDefault("false")
    var isEnabled: Boolean = false,
    var roles: String = "",
//    @OneToOne var customer: Customer? = null
) {

    fun getRoles(): List<Rolename> {
        return if (roles.isEmpty()) mutableListOf<Rolename>()
        else roles.split("_").map { Rolename.valueOf(it) }.toMutableList()
    }

    fun addRole(role: Rolename) {
        if (!roles.contains(role.name)) {
            var newRoles = getRoles().toMutableList()
            newRoles.add(role)
            roles = newRoles.joinToString("_")
        }
    }

    fun removeRole(role: Rolename) {
        if (roles.contains(role.name)) {
            roles = roles.split("_").filter { it != role.name }.joinToString("_")
        }
    }

    fun toDTO() = UserDTO(
        uid = uid!!,
        uname = username,
        pass = password,
        name = name,
        surname = surname,
        mail = email,
        deliveryAddress = deliveryAddress,
        isEn = isEnabled,
        roles = roles
    )

}
