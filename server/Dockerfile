FROM python:3.12

RUN useradd -m server
RUN chmod -R 755 /app

WORKDIR /app

COPY requirements.txt .
RUN pip install -r requirements.txt

COPY main.py .

USER server

CMD ["python3", "main.py"]