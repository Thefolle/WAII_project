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
    @ColumnDefault("false")
    var isAdmin: Boolean = false,
    @ColumnDefault("true")
    var isCustomer: Boolean = true
//    @OneToOne var customer: Customer? = null
) {

    fun getRoles(): List<Rolename> {
        val roles = mutableListOf<Rolename>()
        if (isAdmin) roles.add(Rolename.ADMIN)
        if (isCustomer) roles.add(Rolename.CUSTOMER)
        return roles
    }

    fun addRole(role: Rolename) {
        if (role == Rolename.ADMIN) isAdmin = true
        if (role == Rolename.CUSTOMER) isCustomer = true
    }

    fun removeRole(role: Rolename) {
        if (role == Rolename.ADMIN) isAdmin = false
        if (role == Rolename.CUSTOMER) isCustomer = false
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
        isAdmin = isAdmin,
        isCustomer = isCustomer
    )

}
