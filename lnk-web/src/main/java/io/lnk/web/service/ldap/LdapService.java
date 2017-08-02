package io.lnk.web.service.ldap;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.AndFilter;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.filter.LikeFilter;
import org.springframework.ldap.support.LdapUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author 刘飞 E-mail:liufei_it@126.com
 *
 * @version 1.0.0
 * @since 2017年7月29日 下午2:01:43
 */
public class LdapService {
    private final Logger log = LoggerFactory.getLogger(LdapService.class.getSimpleName());
    @Autowired private LdapTemplate ldapTemplate;
    
    public boolean authenticate(String userName, String password) {
        try {
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", userName));
            return this.ldapTemplate.authenticate(LdapUtils.emptyLdapName(), filter.toString(), password);
        } catch (Throwable e) {
            log.error("ldap auth Error.", e);
        }
        return false;
    }

    public Account getAccount(String userName) {
        try {
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("objectclass", "person")).and(new EqualsFilter("uid", userName));
            List<Account> list = ldapTemplate.search(LdapUtils.emptyLdapName(), filter.toString(), new AccountMapper());
            return CollectionUtils.isEmpty(list) ? null : list.get(0);
        } catch (Throwable e) {
            log.error("ldap getting Error.", e);
        }
        return null;
    }

    public List<Account> queryAccount(String userName) {
        try {
            AndFilter filter = new AndFilter();
            filter.and(new EqualsFilter("objectclass", "person")).and(new LikeFilter("uid", userName));
            return this.ldapTemplate.search(LdapUtils.emptyLdapName(), filter.toString(), new AccountMapper());
        } catch (Throwable e) {
            log.error("ldap query Error.", e);
        }
        return null;
    }
}
