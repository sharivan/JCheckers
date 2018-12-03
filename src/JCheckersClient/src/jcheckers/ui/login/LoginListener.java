package jcheckers.ui.login;

public interface LoginListener {

	void onCancel();

	void onLogin(String username, char[] password);

}
