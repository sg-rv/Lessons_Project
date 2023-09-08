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
                Functions.registrationClients(accounts_path,clients_accounts);
            } else if (inp.equals("2")) {
                String mail = Input.nextLine("Введите почту: ");
                String password = Input.nextLine("Введите пароль: ");
                ClientAccount temp = new ClientAccount(mail, password);
                if (clients_accounts.contains(temp)) {
                    for (ClientAccount clients_account : clients_accounts) {
                        if(clients_account.equals(temp)) {
                            currentAccount = clients_account;
                            break;
                        }
                    }
                    logged_in = true;
                    inp = "3";
                } else {
                    System.out.println("Неверный логин или пароль! ");
                }
            }
        }
        if (logged_in) {
            int port = 3398;
            String host = "localhost";
            try (Socket socket = new Socket(host, port)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                PrintStream writer = new PrintStream(socket.getOutputStream(), true);
                String type_of_request = "";
                assert currentAccount != null;
                while (!type_of_request.equals("4")) {
                    type_of_request = Input.nextLine("""
                            1.Подписать новый контракт
                            2.Добавить счетчик на имеющийся договор
                            3.Передача показаний
                            4.Выход
                            """);
                    switch (type_of_request) {
                        case "1" -> {
                            String service = Input.nextLine("""
                                    1.Электроэнергия
                                    2.Водоснабжение
                                    """);
                            String contract_num = Input.nextLine("Введите номер нового договора: ");
                            Contract new_contract = null;
                            if (service.equals("1")) {
                                new_contract = new Contract(
                                        "E"
                                        + contract_num
                                        + "_"
                                        + currentAccount.getName());
                            } else if (service.equals("2")) {
                                new_contract = new Contract(
                                        "W"
                                        + contract_num
                                        + "_"
                                        + currentAccount.getName());
                            }
                            if (currentAccount.getContracts().contains(new_contract)) {
                                System.out.println("Договор уже существует! ");
                            } else {
                                assert new_contract != null;
                                writer.println("1;" + currentAccount.getName() + ";" + currentAccount.getLogin());
                                oos.writeObject(new_contract);
                                System.out.println("Ваш запрос отправлен!");
                                String signal = reader.readLine();
                                if (signal.equals("OK")) {
                                    System.out.println(reader.readLine());
                                    currentAccount.getContracts().add(new_contract);
                                }
                                else {
                                    System.out.println(reader.readLine());
                                }
                            }
                        }
                        case "2" -> {
                            String contract_num = Input.nextLine("Введите номер договора: ");
                            boolean found = false;
                            for (Contract contract : currentAccount.getContracts()) {
                                if (contract.getContract_num().equals(contract_num)) {
                                    int meter_num = Input.nextInt("Введите номер счетчика: ");
                                    String month_hour = Input.nextLine("""
                                            1.По месяцам
                                            2.По часам
                                            """);
                                    Meter new_meter;
                                    if(month_hour.equals("1")) {
                                        new_meter = new Meter(meter_num, "Month");
                                    } else {
                                        new_meter = new Meter(meter_num, "Hour");
                                    }
                                    if (contract.getMeters().contains(new_meter)) {
                                        System.out.println("Счетчик с таким номером" +
                                                "уже существует в этом договоре!");
                                    } else {
                                        writer.println("2;"
                                                + currentAccount.getName()
                                                + ";"
                                                + currentAccount.getLogin());
                                        writer.println(contract_num);
                                        oos.writeObject(new_meter);
                                        System.out.println("Ваш запрос отправлен!");
                                        String signal = reader.readLine();
                                        if (signal.equals("OK")) {
                                            System.out.println(reader.readLine());
                                            contract.getMeters().add(new_meter);
                                        } else {
                                            System.out.println(reader.readLine());
                                        }
                                    }
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                System.out.println("Договор не найден!");
                            }
                        }
                        case "3" -> {
                            String contract_num = Input.nextLine("Введите номер договора: ");
                            boolean found = false;
                            Contract currentContract = null;
                            for (Contract contract : currentAccount.getContracts()) {
                                if (contract.getContract_num().equals(contract_num)) {
                                    currentContract = contract;
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                System.out.println("Договор не найден! ");
                            } else {
                                String file_or_input = Input.nextLine("""
                                           1. Ввод с консоли
                                           2. Отправить файл
                                           """);
                                if (!file_or_input.equals("1") && !file_or_input.equals("2") ){
                                    System.out.println("Некорректный ввод!");
                                } else {
                                    List<Meter> meters = currentContract.getMeters();
                                    if (file_or_input.equals("1")) {
                                        for (Meter meter : meters) {
                                            if(meter.getMonth_hour().equals("Month")) {
                                                meter.setThis_month(Input.nextInt("Введите показания " +
                                                        "за этот месяц для счетчика " + meter.getMeter_num() + ": "));
                                            } else {
                                                meter.setTotal_days(Input.nextInt("Введите показания " +
                                                        "за все дневное время для счетчика "
                                                        + meter.getMeter_num() + ": "));
                                                meter.setTotal_nights(Input.nextInt("Введите показания " +
                                                        "за все ночное время для счетчика "
                                                        + meter.getMeter_num() + ": "));
                                            }
                                        }
                                        writer.println("3;"
                                                + currentAccount.getName()
                                                + ";"
                                                + currentAccount.getLogin());
                                        writer.println(contract_num);
                                        oos.writeObject(meters);
                                        System.out.println("Ваш запрос отправлен!");
                                        String signal = reader.readLine();
                                        if (signal.equals("OK")) {
                                            System.out.println(reader.readLine());

                                        } else {
                                            System.out.println(reader.readLine());
                                        }
                                    } else {
                                        System.out.println("!23");
                                    }

                                }
                            }
                        }
                    }
                }
                oos.close();
                writer.close();
                reader.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(accounts_path))) {
            oos.writeObject(clients_accounts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    static <T> List<T> deserialize(String filePath) {
        File accounts_file = new File(filePath);
        if (accounts_file.exists()) {
            try (ObjectInputStream oos = new ObjectInputStream(
                    new FileInputStream(filePath))) {
                return (List<T>) oos.readObject();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new ArrayList<>();
    }
    public static void main(String[] args) {
        new Client_App();
    }
}