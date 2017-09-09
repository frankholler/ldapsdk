/*
 * Copyright 2016-2017 Ping Identity Corporation
 * All Rights Reserved.
 */
/*
 * Copyright (C) 2016-2017 Ping Identity Corporation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (GPLv2 only)
 * or the terms of the GNU Lesser General Public License (LGPLv2.1 only)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see <http://www.gnu.org/licenses>.
 */
package com.unboundid.ldap.sdk.unboundidds;



import java.io.BufferedReader;
import java.io.FileReader;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.LinkedHashMap;

import com.unboundid.ldap.sdk.ExtendedResult;
import com.unboundid.ldap.sdk.LDAPConnection;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.ResultCode;
import com.unboundid.ldap.sdk.Version;
import com.unboundid.ldap.sdk.unboundidds.extensions.
            DeregisterYubiKeyOTPDeviceExtendedRequest;
import com.unboundid.ldap.sdk.unboundidds.extensions.
            RegisterYubiKeyOTPDeviceExtendedRequest;
import com.unboundid.util.Debug;
import com.unboundid.util.LDAPCommandLineTool;
import com.unboundid.util.PasswordReader;
import com.unboundid.util.StaticUtils;
import com.unboundid.util.ThreadSafety;
import com.unboundid.util.ThreadSafetyLevel;
import com.unboundid.util.args.ArgumentException;
import com.unboundid.util.args.ArgumentParser;
import com.unboundid.util.args.BooleanArgument;
import com.unboundid.util.args.FileArgument;
import com.unboundid.util.args.StringArgument;

import static com.unboundid.ldap.sdk.unboundidds.UnboundIDDSMessages.*;



/**
 * This class provides a utility that may be used to register a YubiKey OTP
 * device for a specified user so that it may be used to authenticate that user.
 * Alternately, it may be used to deregister one or all of the YubiKey OTP
 * devices that have been registered for the user.
 * <BR>
 * <BLOCKQUOTE>
 *   <B>NOTE:</B>  This class, and other classes within the
 *   {@code com.unboundid.ldap.sdk.unboundidds} package structure, are only
 *   supported for use against Ping Identity, UnboundID, and Alcatel-Lucent 8661
 *   server products.  These classes provide support for proprietary
 *   functionality or for external specifications that are not considered stable
 *   or mature enough to be guaranteed to work in an interoperable way with
 *   other types of LDAP servers.
 * </BLOCKQUOTE>
 */
@ThreadSafety(level=ThreadSafetyLevel.NOT_THREADSAFE)
public final class RegisterYubiKeyOTPDevice
       extends LDAPCommandLineTool
       implements Serializable
{
  /**
   * The serial version UID for this serializable class.
   */
  private static final long serialVersionUID = 5705120716566064832L;



  // Indicates that the tool should deregister one or all of the YubiKey OTP
  // devices for the user rather than registering a new device.
  private BooleanArgument deregister;

  // Indicates that the tool should interactively prompt for the static password
  // for the user for whom the YubiKey OTP device is to be registered or
  // deregistered.
  private BooleanArgument promptForUserPassword;

  // The path to a file containing the static password for the user for whom the
  // YubiKey OTP device is to be registered or deregistered.
  private FileArgument userPasswordFile;

  // The username for the user for whom the YubiKey OTP device is to be
  // registered or deregistered.
  private StringArgument authenticationID;

  // The static password for the user for whom the YubiKey OTP device is to be
  // registered or deregistered.
  private StringArgument userPassword;

  // A one-time password generated by the YubiKey OTP device to be registered
  // or deregistered.
  private StringArgument otp;



  /**
   * Parse the provided command line arguments and perform the appropriate
   * processing.
   *
   * @param  args  The command line arguments provided to this program.
   */
  public static void main(final String... args)
  {
    final ResultCode resultCode = main(args, System.out, System.err);
    if (resultCode != ResultCode.SUCCESS)
    {
      System.exit(resultCode.intValue());
    }
  }



  /**
   * Parse the provided command line arguments and perform the appropriate
   * processing.
   *
   * @param  args       The command line arguments provided to this program.
   * @param  outStream  The output stream to which standard out should be
   *                    written.  It may be {@code null} if output should be
   *                    suppressed.
   * @param  errStream  The output stream to which standard error should be
   *                    written.  It may be {@code null} if error messages
   *                    should be suppressed.
   *
   * @return  A result code indicating whether the processing was successful.
   */
  public static ResultCode main(final String[] args,
                                final OutputStream outStream,
                                final OutputStream errStream)
  {
    final RegisterYubiKeyOTPDevice tool =
         new RegisterYubiKeyOTPDevice(outStream, errStream);
    return tool.runTool(args);
  }



  /**
   * Creates a new instance of this tool.
   *
   * @param  outStream  The output stream to which standard out should be
   *                    written.  It may be {@code null} if output should be
   *                    suppressed.
   * @param  errStream  The output stream to which standard error should be
   *                    written.  It may be {@code null} if error messages
   *                    should be suppressed.
   */
  public RegisterYubiKeyOTPDevice(final OutputStream outStream,
                                  final OutputStream errStream)
  {
    super(outStream, errStream);

    deregister            = null;
    otp                   = null;
    promptForUserPassword = null;
    userPasswordFile      = null;
    authenticationID      = null;
    userPassword          = null;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getToolName()
  {
    return "register-yubikey-otp-device";
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getToolDescription()
  {
    return INFO_REGISTER_YUBIKEY_OTP_DEVICE_TOOL_DESCRIPTION.get(
         UnboundIDYubiKeyOTPBindRequest.UNBOUNDID_YUBIKEY_OTP_MECHANISM_NAME);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public String getToolVersion()
  {
    return Version.NUMERIC_VERSION_STRING;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public void addNonLDAPArguments(final ArgumentParser parser)
         throws ArgumentException
  {
    deregister = new BooleanArgument(null, "deregister", 1,
         INFO_REGISTER_YUBIKEY_OTP_DEVICE_DESCRIPTION_DEREGISTER.get("--otp"));
    deregister.addLongIdentifier("de-register", true);
    parser.addArgument(deregister);

    otp = new StringArgument(null, "otp", false, 1,
         INFO_REGISTER_YUBIKEY_OTP_DEVICE_PLACEHOLDER_OTP.get(),
         INFO_REGISTER_YUBIKEY_OTP_DEVICE_DESCRIPTION_OTP.get());
    parser.addArgument(otp);

    authenticationID = new StringArgument(null, "authID", false, 1,
         INFO_REGISTER_YUBIKEY_OTP_DEVICE_PLACEHOLDER_AUTHID.get(),
         INFO_REGISTER_YUBIKEY_OTP_DEVICE_DESCRIPTION_AUTHID.get());
    authenticationID.addLongIdentifier("authenticationID", true);
    authenticationID.addLongIdentifier("auth-id", true);
    authenticationID.addLongIdentifier("authentication-id", true);
    parser.addArgument(authenticationID);

    userPassword = new StringArgument(null, "userPassword", false, 1,
         INFO_REGISTER_YUBIKEY_OTP_DEVICE_PLACEHOLDER_USER_PW.get(),
         INFO_REGISTER_YUBIKEY_OTP_DEVICE_DESCRIPTION_USER_PW.get(
              authenticationID.getIdentifierString()));
    userPassword.setSensitive(true);
    userPassword.addLongIdentifier("user-password", true);
    parser.addArgument(userPassword);

    userPasswordFile = new FileArgument(null, "userPasswordFile", false, 1,
         null,
         INFO_REGISTER_YUBIKEY_OTP_DEVICE_DESCRIPTION_USER_PW_FILE.get(
              authenticationID.getIdentifierString()),
         true, true, true, false);
    userPasswordFile.addLongIdentifier("user-password-file", true);
    parser.addArgument(userPasswordFile);

    promptForUserPassword = new BooleanArgument(null, "promptForUserPassword",
         INFO_REGISTER_YUBIKEY_OTP_DEVICE_DESCRIPTION_PROMPT_FOR_USER_PW.get(
              authenticationID.getIdentifierString()));
    promptForUserPassword.addLongIdentifier("prompt-for-user-password", true);
    parser.addArgument(promptForUserPassword);


    // At most one of the userPassword, userPasswordFile, and
    // promptForUserPassword arguments must be present.
    parser.addExclusiveArgumentSet(userPassword, userPasswordFile,
         promptForUserPassword);

    // If any of the userPassword, userPasswordFile, or promptForUserPassword
    // arguments is present, then the authenticationID argument must also be
    // present.
    parser.addDependentArgumentSet(userPassword, authenticationID);
    parser.addDependentArgumentSet(userPasswordFile, authenticationID);
    parser.addDependentArgumentSet(promptForUserPassword, authenticationID);
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public void doExtendedNonLDAPArgumentValidation()
         throws ArgumentException
  {
    // If the deregister argument was not provided, then the otp argument must
    // have been given.
    if ((! deregister.isPresent()) && (! otp.isPresent()))
    {
      throw new ArgumentException(
           ERR_REGISTER_YUBIKEY_OTP_DEVICE_NO_OTP_TO_REGISTER.get(
                otp.getIdentifierString()));
    }
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public boolean supportsInteractiveMode()
  {
    return true;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public boolean defaultsToInteractiveMode()
  {
    return true;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  protected boolean supportsOutputFile()
  {
    return true;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  protected boolean defaultToPromptForBindPassword()
  {
    return true;
  }



  /**
   * Indicates whether this tool supports the use of a properties file for
   * specifying default values for arguments that aren't specified on the
   * command line.
   *
   * @return  {@code true} if this tool supports the use of a properties file
   *          for specifying default values for arguments that aren't specified
   *          on the command line, or {@code false} if not.
   */
  @Override()
  public boolean supportsPropertiesFile()
  {
    return true;
  }



  /**
   * Indicates whether the LDAP-specific arguments should include alternate
   * versions of all long identifiers that consist of multiple words so that
   * they are available in both camelCase and dash-separated versions.
   *
   * @return  {@code true} if this tool should provide multiple versions of
   *          long identifiers for LDAP-specific arguments, or {@code false} if
   *          not.
   */
  @Override()
  protected boolean includeAlternateLongIdentifiers()
  {
    return true;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  protected boolean logToolInvocationByDefault()
  {
    return true;
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public ResultCode doToolProcessing()
  {
    // Establish a connection to the Directory Server.
    final LDAPConnection conn;
    try
    {
      conn = getConnection();
    }
    catch (final LDAPException le)
    {
      Debug.debugException(le);
      wrapErr(0, StaticUtils.TERMINAL_WIDTH_COLUMNS,
           ERR_REGISTER_YUBIKEY_OTP_DEVICE_CANNOT_CONNECT.get(
                StaticUtils.getExceptionMessage(le)));
      return le.getResultCode();
    }

    try
    {
      // Get the authentication ID and static password to include in the
      // request.
      final String authID = authenticationID.getValue();

      final byte[] staticPassword;
      if (userPassword.isPresent())
      {
        staticPassword = StaticUtils.getBytes(userPassword.getValue());
      }
      else if (userPasswordFile.isPresent())
      {
        BufferedReader reader = null;
        try
        {
          reader =
               new BufferedReader(new FileReader(userPasswordFile.getValue()));
          staticPassword = StaticUtils.getBytes(reader.readLine());
        }
        catch (final Exception e)
        {
          Debug.debugException(e);
          wrapErr(0, StaticUtils.TERMINAL_WIDTH_COLUMNS,
               ERR_REGISTER_YUBIKEY_OTP_DEVICE_CANNOT_READ_PW.get(
                    StaticUtils.getExceptionMessage(e)));
          return ResultCode.LOCAL_ERROR;
        }
        finally
        {
          if (reader != null)
          {
            try
            {
              reader.close();
            }
            catch (final Exception e)
            {
              Debug.debugException(e);
            }
          }
        }
      }
      else if (promptForUserPassword.isPresent())
      {
        try
        {
          getOut().print(INFO_REGISTER_YUBIKEY_OTP_DEVICE_ENTER_PW.get(authID));
          staticPassword = PasswordReader.readPassword();
        }
        catch (final Exception e)
        {
          Debug.debugException(e);
          wrapErr(0, StaticUtils.TERMINAL_WIDTH_COLUMNS,
               ERR_REGISTER_YUBIKEY_OTP_DEVICE_CANNOT_READ_PW.get(
                    StaticUtils.getExceptionMessage(e)));
          return ResultCode.LOCAL_ERROR;
        }
      }
      else
      {
        staticPassword = null;
      }


      // Construct and process the appropriate register or deregister request.
      if (deregister.isPresent())
      {
        final DeregisterYubiKeyOTPDeviceExtendedRequest r =
             new DeregisterYubiKeyOTPDeviceExtendedRequest(authID,
                  staticPassword, otp.getValue());

        ExtendedResult deregisterResult;
        try
        {
          deregisterResult = conn.processExtendedOperation(r);
        }
        catch (final LDAPException le)
        {
          deregisterResult = new ExtendedResult(le);
        }

        if (deregisterResult.getResultCode() == ResultCode.SUCCESS)
        {
          if (otp.isPresent())
          {
            wrapOut(0, StaticUtils.TERMINAL_WIDTH_COLUMNS,
                 INFO_REGISTER_YUBIKEY_OTP_DEVICE_DEREGISTER_SUCCESS_ONE.get(
                      authID));
          }
          else
          {
            wrapOut(0, StaticUtils.TERMINAL_WIDTH_COLUMNS,
                 INFO_REGISTER_YUBIKEY_OTP_DEVICE_DEREGISTER_SUCCESS_ALL.get(
                      authID));
          }
          return ResultCode.SUCCESS;
        }
        else
        {
          wrapErr(0, StaticUtils.TERMINAL_WIDTH_COLUMNS,
               ERR_REGISTER_YUBIKEY_OTP_DEVICE_DEREGISTER_FAILED.get(authID,
                    String.valueOf(deregisterResult)));
          return deregisterResult.getResultCode();
        }
      }
      else
      {
        final RegisterYubiKeyOTPDeviceExtendedRequest r =
             new RegisterYubiKeyOTPDeviceExtendedRequest(authID, staticPassword,
                  otp.getValue());

        ExtendedResult registerResult;
        try
        {
          registerResult = conn.processExtendedOperation(r);
        }
        catch (final LDAPException le)
        {
          registerResult = new ExtendedResult(le);
        }

        if (registerResult.getResultCode() == ResultCode.SUCCESS)
        {
          wrapOut(0, StaticUtils.TERMINAL_WIDTH_COLUMNS,
               INFO_REGISTER_YUBIKEY_OTP_DEVICE_REGISTER_SUCCESS.get(authID));
          return ResultCode.SUCCESS;
        }
        else
        {
          wrapErr(0, StaticUtils.TERMINAL_WIDTH_COLUMNS,
               ERR_REGISTER_YUBIKEY_OTP_DEVICE_REGISTER_FAILED.get(authID,
                    String.valueOf(registerResult)));
          return registerResult.getResultCode();
        }
      }
    }
    finally
    {
      conn.close();
    }
  }



  /**
   * {@inheritDoc}
   */
  @Override()
  public LinkedHashMap<String[],String> getExampleUsages()
  {
    final LinkedHashMap<String[],String> exampleMap =
         new LinkedHashMap<String[],String>(2);

    String[] args =
    {
      "--hostname", "server.example.com",
      "--port", "389",
      "--bindDN", "uid=admin,dc=example,dc=com",
      "--bindPassword", "adminPassword",
      "--authenticationID", "u:test.user",
      "--userPassword", "testUserPassword",
      "--otp", "abcdefghijklmnopqrstuvwxyzabcdefghijklmnopqr"
    };
    exampleMap.put(args,
         INFO_REGISTER_YUBIKEY_OTP_DEVICE_EXAMPLE_REGISTER.get());

    args = new String[]
    {
      "--hostname", "server.example.com",
      "--port", "389",
      "--bindDN", "uid=admin,dc=example,dc=com",
      "--bindPassword", "adminPassword",
      "--deregister",
      "--authenticationID", "dn:uid=test.user,ou=People,dc=example,dc=com"
    };
    exampleMap.put(args,
         INFO_REGISTER_YUBIKEY_OTP_DEVICE_EXAMPLE_DEREGISTER.get());

    return exampleMap;
  }
}
