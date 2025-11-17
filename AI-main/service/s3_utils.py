import boto3, os
from urllib.parse import urlparse
from dotenv import load_dotenv

load_dotenv()

AWS_BUCKET = os.getenv("AWS_BUCKET")
AWS_REGION = os.getenv("AWS_REGION")

s3_client = boto3.client(
    "s3",
    aws_access_key_id=os.getenv("AWS_ACCESS_KEY"),
    aws_secret_access_key=os.getenv("AWS_ACCESS_SECRETKEY"),
    region_name=AWS_REGION
)

def download_private_image(photo_url: str) -> bytes:
    """
    S3 private 이미지 다운로드.
    public URL이어도 key만 추출해서 boto3로 다운로드 가능.
    """
    parsed = urlparse(photo_url)
    key = parsed.path.lstrip("/")  # "/abc.png" -> "abc.png"

    obj = s3_client.get_object(Bucket=AWS_BUCKET, Key=key)
    return obj["Body"].read()
