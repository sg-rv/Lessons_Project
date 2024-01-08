package lesson23_meters;

import scn.Input;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

public class ClientService {

    public void registration(String accounts_path, List<ClientAccount> clients_accounts) {
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
                System.out.println(e.getMessage());
            }
        } else {
            System.out.println("Логин занят либо логин или пароль не соответствуют требованиям!");
        }
    }

    public void func_1(ClientAccount currentAccount, BufferedReader reader,
                        ObjectOutputStream oos, PrintStream writer) throws IOException {
        String service = Input.nextLine(
                                    """
                                    1.Электроэнергия
                                    2.Водоснабжение
                                    """);
        String contract_num = Input.nextLine("Введите номер нового договора: ");
        Contract new_contract = getNewContract(service, contract_num, currentAccount);
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

    public void func_2(ClientAccount currentAccount, BufferedReader reader,
                       ObjectOutputStream oos, PrintStream writer) throws IOException {
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

    public void func_3(ClientAccount currentAccount, BufferedReader reader,
                       ObjectOutputStream oos, PrintStream writer) throws IOException {
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

                } else {
                    String path_meter_values = Input.nextLine("Введите путь к файлу: ");
                    if(!Files.exists(Path.of(path_meter_values))) {
                        System.out.println("Файл не найден!");
                    } else {
                        try (BufferedReader br = new BufferedReader(
                                new FileReader(path_meter_values))) {
                            while (br.ready()) {
                                for (Meter meter : meters) {
                                    if (meter.getMonth_hour().equals("Month")) {
                                        meter.setThis_month(Integer.parseInt(br.readLine()));
                                    } else {
                                        meter.setTotal_days(Integer.parseInt(br.readLine()));
                                        meter.setTotal_nights(Integer.parseInt(br.readLine()));
                                    }
                                }
                            }
                        } catch (Exception e){
                            System.out.println(e.getMessage());
                        }
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
            }
        }
    }
    public void func_4(ClientAccount currentAccount, BufferedReader reader,
                       ObjectOutputStream oos, PrintStream writer) throws IOException {
        String contract_to_delete_num = Input.nextLine("Введите номер договора: ");
        Contract contract_to_delete = null;
        boolean found = false;
        for (Contract contract : currentAccount.getContracts()) {
            if (contract.getContract_num().equals(contract_to_delete_num)) {
                contract_to_delete = contract;
                found = true;
                break;
            }
        }
        if (!found) {
            System.out.println("Договор не найден!");
        } else {
            writer.println("4;" + currentAccount.getName() + ";" + currentAccount.getLogin());
            oos.writeObject(contract_to_delete);
            System.out.println("Ваш запрос отправлен!");
            String signal = reader.readLine();
            if (signal.equals("OK")) {
                System.out.println(reader.readLine());
                currentAccount.getContracts().remove(contract_to_delete);
            }
            else {
                System.out.println(reader.readLine());
            }
        }
    }
    public void func_5(ClientAccount currentAccount, BufferedReader reader,
                       ObjectOutputStream oos, PrintStream writer) throws IOException {
        String current_contract_num = Input.nextLine("Введите номер договора: ");
        int current_meter_num = Input.nextInt("Введите номер счетчика: ");
        Meter current_meter = null;
        Contract current_contract = null;
        for (Contract contract : currentAccount.getContracts()) {
            for (Meter meter : contract.getMeters()) {
                if (meter.getMeter_num() == current_meter_num) {
                    current_contract = contract;
                    current_meter = meter;
                }
            }
        }
        if (current_meter == null) {
            System.out.println("Счетчик не найден!");
        } else {
            writer.println("5;"
                    + currentAccount.getName()
                    + ";"
                    + currentAccount.getLogin());
            writer.println(current_contract_num);
            oos.writeObject(current_meter);
            System.out.println("Ваш запрос отправлен!");
            String signal = reader.readLine();
            if (signal.equals("OK")) {
                System.out.println(reader.readLine());
                current_contract.getMeters().remove(current_meter);
            } else {
                System.out.println(reader.readLine());
            }
        }
    }
    private static Contract getNewContract(String service, String contract_num, ClientAccount currentAccount) {
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
        return new_contract;
    }
}