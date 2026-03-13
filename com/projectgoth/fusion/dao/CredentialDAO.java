package com.projectgoth.fusion.dao;

import com.projectgoth.fusion.authentication.PasswordType;
import com.projectgoth.fusion.authentication.domain.PersistedCredential;
import java.util.List;
import org.keyczar.Crypter;

public interface CredentialDAO {
   PersistedCredential getCredential(int var1, PasswordType var2);

   PersistedCredential getCredentialFromNewOrOldTable(int var1, PasswordType var2, Crypter var3);

   Byte[] availableCredentialTypes(int var1);

   List<PersistedCredential> getCredentialsByTypes(int var1, PasswordType[] var2);

   List<PersistedCredential> getAllCredentials(int var1);

   List<PersistedCredential> getAllCredentialsFromMaster(int var1);

   List<PersistedCredential> getAllCredentialsFromOldTable(int var1);

   int createCredential(PersistedCredential var1);

   int[] createCredentials(List<PersistedCredential> var1);

   int updateCredentialPassword(PersistedCredential var1);

   int updateCredentialUsernameAndPassword(PersistedCredential var1);

   int removeCredential(PersistedCredential var1);

   int userIDForUsername(String var1);

   boolean credentialExists(String var1, PasswordType var2);

   List<PersistedCredential> getCredentialsByUsernameAndPasswordType(String var1, PasswordType var2);
}
