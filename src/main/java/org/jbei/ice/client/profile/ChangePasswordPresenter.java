package org.jbei.ice.client.profile;

public class ChangePasswordPresenter {

    public interface IChangePasswordView {

        String getPassword();

        String getPasswordConfirm();

        void passwordError(String msg);

        void passwordConfirmError(String msg);
    }

    private final IChangePasswordView view;

    public ChangePasswordPresenter(IChangePasswordView view) {
        this.view = view;
    }

    public boolean validates() {

        String password = view.getPassword();
        String confirm = view.getPasswordConfirm();

        String passwordError = null;

        // non empty password and must be at least 6 xters
        if (password.isEmpty()) {
            passwordError = "Password cannot be empty";
        } else if (password.trim().length() < 6) {
            passwordError = "Password must have at least six (6) characters";
        }

        if (passwordError != null) {
            view.passwordError(passwordError);
            return false;
        }

        // confirm must match
        if (password.trim().equals(confirm)) {
            view.passwordError(null);
            view.passwordConfirmError(null);
            return true;
        }

        view.passwordError(null);
        view.passwordConfirmError("Passwords do not match");
        return false;
    }
}
