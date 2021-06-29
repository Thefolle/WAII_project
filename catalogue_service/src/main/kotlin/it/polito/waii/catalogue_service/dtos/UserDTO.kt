package it.polito.waii.catalogue_service.dtos

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import javax.validation.constraints.Pattern

class UserDTO(
    var uid: Long?,
    @Pattern(regexp = "^.{8,}$")
    var uname: String,
    @Pattern(regexp = ".*@.*", message = "Invalid email.")
    var mail: String,
    var pass: String,
    var name: String,
    var surname: String,
    var deliveryAddress: String,
    var isEn:Boolean,
    var roles:String
): UserDetails {


    /**
     * Returns the password used to authenticate the user.
     * @return the password
     */
    override fun getPassword(): String {
        return pass
    }

    /**
     * Returns the username used to authenticate the user. Cannot return
     * `null`.
     * @return the username (never `null`)
     */
    override fun getUsername(): String {
        return uname
    }

    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return roles
            .split("_")
            .map { GrantedAuthority { it } }
            .toMutableList()
    }

    /**
     * Indicates whether the user is enabled or disabled. A disabled user cannot be
     * authenticated.
     * @return `true` if the user is enabled, `false` otherwise
     */
    override fun isEnabled(): Boolean {
        return isEn
    }

    override fun isAccountNonExpired(): Boolean {
        return true
    }

    override fun isAccountNonLocked(): Boolean {
        return true
    }

    override fun isCredentialsNonExpired(): Boolean {
        return true
    }

    fun toJson(): String {
        val gson = GsonBuilder().setPrettyPrinting().create()
        val json = gson.toJson(this)
        println(json)
        return json
    }
}
