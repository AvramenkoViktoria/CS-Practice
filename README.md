# Client-server applications in Java

## Overview

This commit contains the implementation for **Laboratory Works 1 and 2**

### Bonus: Scalability Support

The system supports easy horizontal scaling by configuring the number of worker threads for each stage of the pipeline.

**Demonstration of the bonus requirement** can be found in `Main.java`:

- **2 Receivers**
- **2 Decoders**
- **4 Processors**
- **3 Encoders**
- **5 Senders**

## Configuration

The project uses environment variables for cryptographic settings.

Create a `.env` file in the project root with the following content:

```env
CRYPTO_ALGORITHM=AES
CRYPTO_KEY=1234567890123456
```