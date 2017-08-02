package io.lnk.web.service.ldap;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.ldap.core.AttributesMapper;

public class AccountMapper implements AttributesMapper<Account> {

	@Override
	public Account mapFromAttributes(Attributes attributes) throws NamingException {
		Account account = new Account();
		NamingEnumeration<String> ids = attributes.getIDs();
		while (ids.hasMore()) {
			try {
				String id = ids.next();
				Object attr = attributes.get(id).get();
				BeanUtils.copyProperty(account, id, attr);
			} catch (Throwable e) {}
		}
		return account;
	}
}
