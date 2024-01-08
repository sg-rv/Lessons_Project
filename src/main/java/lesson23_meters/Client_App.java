package lesson23_meters;

import scn.Input;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Client_App
{
    Client_App() {
        String accounts_path = "D:\\SE\\JavaProjects\\Lessons_Project\\" +
                "src\\main\\java\\lesson23_meters\\clients_accounts.csv";
        List<ClientAccount> clients_accounts = deserialize(accounts_path);
        ClientService clientService = new ClientService();
        boolean logged_in = false;
        String inp = "";
        ClientAccount currentAccount = null;
        while (!inp.equals("3")) {
            inp = Input.nextLine("""
                    1. Регистрация
                    2. Вход в личный кабинет
                    3. Выход
                    """);
            if (inp.equals("1")) {
                clientService.registration(accounts_path,clients_accounts);
            } else if (inp.equals("2")) {
                String login = Input.nextLine("Введите логин: ");
                String password = Input.nextLine("Введите пароль: ");
                for (ClientAccount account : clients_accounts) {
                    if(account.getPass().equals(password) && account.getLogin().equals(login)) {
                        currentAccount = account;
                        logged_in = true;
                        break;
                    }
                }
                if (logged_in) {
                    inp = "3";
                } else {
                    System.out.println("Неверный логин или пароль! ");
                }
            }
        }
        if (logged_in) {
            int port = 3498;
            String host = "localhost";
            try (Socket socket = new Socket(host, port)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                PrintStream writer = new PrintStream(socket.getOutputStream(), true);
                String type_of_request = "";
                while (!type_of_request.equals("6")) {
                    type_of_request = Input.nextLine("""
                            1.Подписать новый контракт
                            2.Добавить счетчик на имеющийся договор
                            3.Передача показаний
                            4.Аннулировать договор
                            5.Удалить счетчик по гомеру договора
                            6.Выход
                            """);
                    for (Contract contract : currentAccount.getContracts()) {
                        for (Meter meter : contract.getMeters()) {
                            System.out.println(meter.getMeter_num());
                        }
                    }
                    switch (type_of_request) {
                        case "1" -> clientService.func_1(currentAccount, reader, oos, writer);
                        case "2" -> clientService.func_2(currentAccount, reader, oos, writer);
                        case "3" -> clientService.func_3(currentAccount, reader, oos, writer);
                        case "4" -> clientService.func_4(currentAccount, reader, oos, writer);
                        case "5" -> clientService.func_5(currentAccount, reader, oos, writer);
                    }
                }
                oos.close();
                writer.close();
                reader.close();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(accounts_path))) {
            oos.writeObject(clients_accounts);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }



    static <T> List<T> deserialize(String filePath) {
        File accounts_file = new File(filePath);
        if (accounts_file.exists()) {
            try (ObjectInputStream oos = new ObjectInputStream(
                    new FileInputStream(filePath))) {
                return (List<T>) oos.readObject();
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
        return new ArrayList<>();
    }
    public static void main(String[] args) {
        new Client_App();
    }
}