package com.certainshop.service;

import com.certainshop.entity.BienThe;
import com.certainshop.entity.HinhAnhBienThe;
import com.certainshop.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SanPhamServiceTest {

    private final SanPhamRepository sanPhamRepository = mock(SanPhamRepository.class);
    private final BienTheRepository bienTheRepository = mock(BienTheRepository.class);
    private final DanhMucRepository danhMucRepository = mock(DanhMucRepository.class);
    private final ThuongHieuRepository thuongHieuRepository = mock(ThuongHieuRepository.class);
    private final KichThuocRepository kichThuocRepository = mock(KichThuocRepository.class);
    private final MauSacRepository mauSacRepository = mock(MauSacRepository.class);
    private final ChatLieuRepository chatLieuRepository = mock(ChatLieuRepository.class);
    private final HinhAnhBienTheRepository hinhAnhRepository = mock(HinhAnhBienTheRepository.class);
    private SanPhamService service;

    @TempDir
    Path uploadDir;

    @BeforeEach
    void setUp() {
        service = new SanPhamService(sanPhamRepository, bienTheRepository, danhMucRepository,
                thuongHieuRepository, kichThuocRepository, mauSacRepository,
                chatLieuRepository, hinhAnhRepository);
        when(bienTheRepository.findById(1L)).thenReturn(Optional.of(BienThe.builder().id(1L).build()));
        when(hinhAnhRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void storesDecodedImageInsideConfiguredDirectory() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "jpg", output);
        MockMultipartFile file = new MockMultipartFile(
                "file", "../../outside.jpg", "image/jpeg", output.toByteArray());

        HinhAnhBienThe saved = service.uploadAnhBienThe(1L, file, false, uploadDir.toString());

        try (var files = Files.list(uploadDir)) {
            assertEquals(1, files.count());
        }
        assertTrue(saved.getDuongDan().matches("/uploads/images/[0-9a-f-]+\\.jpg"));
        assertFalse(Files.exists(uploadDir.getParent().resolve("outside.jpg")));
    }

    @Test
    void rejectsNonImageContentWithImageExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "fake.jpg", "image/jpeg", "<script>alert(1)</script>".getBytes());

        assertThrows(IllegalArgumentException.class,
                () -> service.uploadAnhBienThe(1L, file, false, uploadDir.toString()));
        verify(hinhAnhRepository, never()).save(any());
    }
}
