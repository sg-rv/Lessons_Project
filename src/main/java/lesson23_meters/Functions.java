package lesson23_meters;

import scn.Input;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.regex.Pattern;

public class Functions {
    public static ClientAccount find_client(List<ClientAccount> clients, String clientLogin) {
        for (ClientAccount client : clients) {
            if (client.getLogin().equals(clientLogin)) {
                return client;
            }
        }
        return null;
    }
    public static Contract find_contract(List<ClientAccount> clients, String clientLogin, String contract_num) {
        ClientAccount currentClient = find_client(clients, clientLogin);
        if (currentClient != null) {
            for (Contract contract : currentClient.getContracts()) {
                if (contract.getContract_num().equals(contract_num)) {
                    return contract;
                }
            }
        }
        return null;
    }
    public static void registration(String accounts_path, List<Account> accounts) {
        String login = "";
        String password = "";
        Pattern login_pattern = Pattern.compile("\\w{6,}");
        Pattern pass_pattern = Pattern.compile("(\\w*(?=.*[A-Z])+\\w*){6,}");
        boolean login_ok = false;
        boolean pass_ok = false;
        while (!login_ok && !pass_ok) {
            login = Input.nextLine("Введите логин: ");
            List<String> logins = accounts.stream()
                    .map(Account::getLogin)
                    .toList();
            if (login_pattern.matcher(login).find() && !logins.contains(login)) {
                login_ok = true;
            }
            password = Input.nextLine("Введите пароль: ");
            if (pass_pattern.matcher(password).find()) {
                pass_ok = true;
            }
        }
        if (login_ok && pass_ok) {
            System.out.println("Регистрация успешна!");
            accounts.add(new Account(login, password));
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(accounts_path))) {
                oos.writeObject(accounts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Логин занят!");
        }
    }
    public static void registrationClients(String accounts_path, List<ClientAccount> clients_accounts) {
        String mail = "";
        String password = "";
        String name = "";
        Pattern pass_pattern = Pattern.compile("(\\w*(?=.*[A-Z])){6,}");
        Pattern mail_pattern = Pattern.compile("[a-z0-9._-]+@[a-z]+\\.[a-z]");
        boolean login_ok = false;
        boolean pass_ok = false;
        while (!login_ok && !pass_ok) {
            mail = Input.nextLine("Введите почту: ");
            List<String> mails = clients_accounts.stream()
                    .map(ClientAccount::getLogin)
                    .toList();
            if (mail_pattern.matcher(mail).find() && !mails.contains(mail)) {
                login_ok = true;
            }
            password = Input.nextLine("Введите пароль: ");
            if (pass_pattern.matcher(password).find()) {
                pass_ok = true;
            }
            name = Input.nextLine("Введите имя: ");
        }
        if (login_ok && pass_ok) {
            System.out.println("Регистрация успешна!");
            clients_accounts.add(new ClientAccount(mail, password, name));
            try (ObjectOutputStream oos = new ObjectOutputStream(
                    new FileOutputStream(accounts_path))) {
                oos.writeObject(clients_accounts);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("Логин занят либо логин или пароль не соответствуют требованиям!");
        }
    }
}
