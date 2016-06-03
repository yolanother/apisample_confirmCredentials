package com.nvidia.testcredentialvalidation;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import java.io.IOException;

public class TestAuthenticate extends AppCompatActivity {
    private static final String GOOGLE_ACCOUNT = "com.google";
    private static final int REQUEST_CHOOSE_ACCOUNT = 0;
    private static final int REQUEST_GRANT_PERMISSION = 1;

    private TextView mResultsText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_authenticate);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!requestPermissionIfNeeded()) {
                    chooseAccount();
                }
            }
        });

        mResultsText = (TextView) findViewById(R.id.results);
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void chooseAccount() {
        mResultsText.setText("Choosing account to verify...");
        Intent intent;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            intent = AccountManager.newChooseAccountIntent(null, null, new String[]{GOOGLE_ACCOUNT},
                    null, null, null, null);
        } else {
            intent = AccountManager.newChooseAccountIntent(null, null, new String[]{GOOGLE_ACCOUNT}, true, null, null, null, null);
        }
        startActivityForResult(intent, REQUEST_CHOOSE_ACCOUNT);
    }

    protected void onActivityResult(final int requestCode, final int resultCode,
                                    final Intent data) {
        if (requestCode == REQUEST_CHOOSE_ACCOUNT && resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            verifyGoogleAccount(getAccount(accountName));
        } else {
            mResultsText.setText("");
        }
    }

    private void verifyGoogleAccount(Account account) {
        if (null != account) {
            mResultsText.setText("Authenticating...");
            final AccountManager accountManager = AccountManager.get(this);
            accountManager.confirmCredentials(account, null, this, new AccountManagerCallback<Bundle>() {
                @Override
                public void run(AccountManagerFuture<Bundle> future) {
                    String message = "";

                    message += "Done State: " + future.isDone() + "\n";
                    message += "Cancelled State: " + future.isCancelled() + "\n";
                    try {
                        message += "Bundle contents: " + future.getResult() + "\n";
                    } catch (OperationCanceledException e) {
                        message += "Operation canceled: " + e.getMessage() + "\n";
                    } catch (IOException e) {
                        message += "IOException: " + e.getMessage() + "\n";
                    } catch (AuthenticatorException e) {
                        message += "AuthenticatorException: " + e.getMessage() + "\n";
                    }

                    mResultsText.setText(message);
                }
            }, null);
        } else {
            mResultsText.setText("No Google accounts associated with this device. Please add a google account before trying this test app.");
        }
    }

    private Account getAccount(String name) {
        final AccountManager accountManager = AccountManager.get(this);
        final Account[] accounts = accountManager.getAccountsByType(GOOGLE_ACCOUNT);
        Account account = null;
        for (Account a : accounts) {
            if (TextUtils.equals(a.name, name)) {
                account = a;
                break;
            }
        }
        return account;
    }

    private boolean requestPermissionIfNeeded() {
        boolean needed = ContextCompat.checkSelfPermission(this,
                Manifest.permission.GET_ACCOUNTS)
                != PackageManager.PERMISSION_GRANTED;
        if (needed) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.GET_ACCOUNTS},
                    REQUEST_GRANT_PERMISSION);
        }

        return needed;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_GRANT_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    chooseAccount();
                } else {
                    mResultsText.setText("User denied access to get accounts.");
                }
                break;
        }
    }
}
